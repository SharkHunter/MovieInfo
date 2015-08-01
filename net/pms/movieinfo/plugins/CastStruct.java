package net.pms.movieinfo.plugins;

public class CastStruct {

	public String Actor = null;
	public String Picture = null;
	public String Character = null;

	public String toString() {
		return "Actor: " + (Actor == null ? "None" : Actor) + (Character == null ? "" : " as " + Character) +
				(Picture == null ? ", No picture" : ", Picture: \"" + Picture + "\"");
	}
}
