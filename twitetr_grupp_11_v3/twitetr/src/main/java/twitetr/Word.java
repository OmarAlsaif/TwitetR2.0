package twitetr;

/**
 * 
 * @author Lukas Simpel klass för att lagra mer information om varje ord i
 *         strängen som skickas tillbaka av LIBRIS
 */

public class Word {

	private String word;
	private long index;

	public Word(long index, String word) {
		this.word = word;
		this.index = index;
	}

	public String getWord() {
		return word;
	}

	public long getIndex() {
		return index;
	}

}
