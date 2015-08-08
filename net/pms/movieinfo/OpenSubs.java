package net.pms.movieinfo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSubs {
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenSubs.class);
	private static final long TOKEN_AGE_TIME = 10 * 60 * 1000; // 10 minutes

	/**
	 * Size of the chunks that will be hashed in bytes (64 KB)
	 */
	private static final int HASH_CHUNK_SIZE = 64 * 1024;

	private static final String OPENSUBS_URL = "http://api.opensubtitles.org/xml-rpc";
	private static String token = null;
	private static long tokenAge;

	public static String computeHash(File file) throws IOException {
		long size = file.length();
		FileInputStream fis = new FileInputStream(file);
		return computeHash(fis, size);
	}

	public static String computeHash(InputStream stream, long length) throws IOException {

		int chunkSizeForFile = (int) Math.min(HASH_CHUNK_SIZE, length);

		// buffer that will contain the head and the tail chunk, chunks will overlap if length is smaller than two chunks
		byte[] chunkBytes = new byte[(int) Math.min(2 * HASH_CHUNK_SIZE, length)];

		DataInputStream in = new DataInputStream(stream);

		// first chunk
		in.readFully(chunkBytes, 0, chunkSizeForFile);

		long position = chunkSizeForFile;
		long tailChunkPosition = length - chunkSizeForFile;

		// seek to position of the tail chunk, or not at all if length is smaller than two chunks
		while (position < tailChunkPosition && (position += in.skip(tailChunkPosition - position)) >= 0);

		// second chunk, or the rest of the data if length is smaller than two chunks
		in.readFully(chunkBytes, chunkSizeForFile, chunkBytes.length - chunkSizeForFile);

		long head = computeHashForChunk(ByteBuffer.wrap(chunkBytes, 0, chunkSizeForFile));
		long tail = computeHashForChunk(ByteBuffer.wrap(chunkBytes, chunkBytes.length - chunkSizeForFile, chunkSizeForFile));

		in.close();
		return String.format("%016x", length + head + tail);
	}

	private static long computeHashForChunk(ByteBuffer buffer) {

		LongBuffer longBuffer = buffer.order(ByteOrder.LITTLE_ENDIAN).asLongBuffer();
		long hash = 0;

		while (longBuffer.hasRemaining()) {
			hash += longBuffer.get();
		}

		return hash;
	}

	public static String postPage(URLConnection connection, String query) throws IOException {
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setDefaultUseCaches(false);
		connection.setRequestProperty("Content-Type", "text/xml");
		connection.setRequestProperty("Content-Length", "" + query.length());
		//LOGGER.debug("opensub query "+query);
		// open up the output stream of the connection
		if (!StringUtils.isEmpty(query)) {
			DataOutputStream output = new DataOutputStream(connection.getOutputStream());
			output.writeBytes(query);
			output.flush();
			output.close();
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder page = new StringBuilder();
		String str;
		while ((str = in.readLine()) != null) {
			page.append(str.trim());
			page.append("\n");
		}
		in.close();
		//LOGGER.debug("opensubs result page "+page.toString());
		return page.toString();
	}

	private static boolean tokenIsYoung() {
		long now = System.currentTimeMillis();
		return ((now - tokenAge) < TOKEN_AGE_TIME);
	}

	private static void login() throws IOException {
		if ((token != null) && tokenIsYoung()) {
			return;
		}
		URL url = new URL(OPENSUBS_URL);
		String req = "<methodCall>\n<methodName>LogIn</methodName>\n<params>\n<param>\n<value><string/></value>\n</param>\n" +
			"<param>\n" +
			"<value><string/></value>\n</param>\n<param>\n<value><string/></value>\n" +
			"</param>\n<param>\n<value><string>OS Test User Agent</string></value>\n</param>\n" +
			"</params>\n" +
			"</methodCall>\n";
		Pattern re = Pattern.compile("token.*?<string>([^<]+)</string>", Pattern.DOTALL);
		Matcher m = re.matcher(postPage(url.openConnection(), req));
		if (m.find()) {
			token = m.group(1);
			tokenAge = System.currentTimeMillis();
		}
	}

	public static String fetchImdbId(File f) throws IOException {
		return fetchImdbId(getHash(f));
	}

	public static String fetchImdbId(String hash) throws IOException {
		LOGGER.debug("{MovieInfo} Fetching IMDB id for hash {}", hash);
		login();
		if (token == null) {
			return "";
		}
		URL url = new URL(OPENSUBS_URL);
		String req = "<methodCall>\n<methodName>CheckMovieHash2</methodName>\n" +
			"<params>\n<param>\n<value><string>" + token + "</string></value>\n</param>\n" +
			"<param>\n<value>\n<array>\n<data>\n<value><string>" + hash + "</string></value>\n" +
			"</data>\n</array>\n</value>\n</param>" +
			"</params>\n</methodCall>\n";
		Pattern re = Pattern.compile("MovieImdbID.*?<string>[^<]+</string>.*?MovieKind.*?<string>tv series</string>.*?"+
				"MovieImdbID.*?<string>([^<]+)</string>.*?MovieKind.*?<string>episode</string>",
				Pattern.DOTALL);
		Pattern re2 = Pattern.compile("MovieImdbID.*?<string>([^<]+)</string>",Pattern.DOTALL);
		//Pattern re1 = Pattern.compile("MovieKind.*?<string>([^<]+)</string>", Pattern.DOTALL);
		String page = postPage(url.openConnection(), req);
		Matcher m = re.matcher(page);
		if (m.find()) {
			return m.group(1);
		}
		m = re2.matcher(page);
		if(m.find()) {
			return m.group(1);
		}
		return "";
	}

	public static String getHash(File f) throws IOException {
		return computeHash(f);
	}
}
