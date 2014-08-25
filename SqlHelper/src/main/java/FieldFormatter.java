import java.io.FileInputStream;
import java.io.ObjectInputStream.GetField;
import java.util.Scanner;

public class FieldFormatter {

	private static final String SPLITTER = "_";

	public static void main(String[] args) {
		FieldFormatter app = new FieldFormatter();
		app.format("fields.txt");
	}

	public void format(String file) {
		Scanner in = new Scanner(getClass().getResourceAsStream("fields.txt"));
		while (in.hasNextLine()) {
			String field = in.nextLine();

			while (field.contains(SPLITTER)) {
				int index = field.indexOf(SPLITTER);
				String head = field.substring(0, index);
				String tail = "";
				if (index + 1 < field.length()) {
					tail = field.substring(index + 1);
				}
				field = head + upperHead(tail);
			}
			
			field = lowHead(field);
			System.out.println(field);
		}
	}
	
	private String lowHead(String s) {
		return s.replaceFirst("^\\S?", s.substring(0, 1).toLowerCase());
	}
	
	private String upperHead(String s) {
		return s.replaceFirst("^\\[w^[\\d]]", s.substring(0, 1).toUpperCase());
	}

}
