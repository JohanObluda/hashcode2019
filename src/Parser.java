import java.util.*;

public class Parser {
	private Parser() {}

	public static List<Photo> parse(Stream<String> linesStream) {
		int identifierCount = 0;

		String[] lines = lineStream.toArray();
		int photoLines = Integer.parseInt(lines[0]);
		assert photoLines == lines.length - 1;

		List<Photo> result = new LinkedList<>();
		for (int i = 1; i < lines.length; ++i) {
			if (lines[i].trim().isEmpty())
				continue; // We'll just happily continue

			List photoTags = Arrays.asList(lines[i].split(' '));
			assert photoTags.length() > 0;

			// Get vertical property
			boolean vertical = photoTags.get(0).equals("V");
			// Get tags (remove alignment property)
			photoTags.removeAt(0);
			result.add(new Photo(vertical, photoTags, identifierCount++));
		}

		return result;
	}

	public static String toOutput(List<Slide> slides) {
		StringBuilder result = new StringBuilder();
		result.append(slides.size()).append('\n');
		for (Slide slide : slides) {
			result.append(slide.toString()).append('\n');
		}
		return result.toString();
	}

	public static String toDebugOutput(List<Slide> slides) {
		StringBuilder result = new StringBuilder();
		result.append(slides.size()).append('\n');
		for (Slide slide : slides) {
			result.append(slide.toString()).append('[');
			for (String tag : slide.getTags()) {
				result.append(' ').append(tag);
			}

			result.append(" ]\n");
		}
		return result.toString();
	}
}
