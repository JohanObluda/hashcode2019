import java.util.*;
import java.util.stream.*;

public final class Solver {
	private Solver() {
	}

	private static int mergeTagLists(final List<String> l0, final List<String> l1) {
		return (int) l0.stream().filter(l1::contains).count();
	}

	public static int slideInterest(final Slide slide0, final Slide slide1) {
		final int mergedTags = mergeTagLists(slide0.getTags(), slide1.getTags());
		return Math.min(slide0.getTags().size() - mergedTags,
				Math.min(mergedTags, slide1.getTags().size() - mergedTags));
	}

	public static List<Slide> getHorizontalSlides(final List<Photo> photos) {
		return photos.stream().filter(Photo::isHorizontal).map(photo -> new SlideHorizontal(photo))
				.collect(Collectors.toList());
	}

	public static List<Slide> getVerticalSlidesLastFirst(final List<Photo> photos) {
		List<Photo> verticalPhotos = photos.stream().filter(Photo::isVertical)
				.sorted((photo0, photo1) -> Integer.compare(photo0.getTags().size(), photo1.getTags().size()))
				.collect(Collectors.toList());
		// Last with first ...
		final List<Slide> slides = new ArrayList<>(verticalPhotos.size() / 2);
		int firstIndex = 0, lastIndex = verticalPhotos.size() - 1;

		while (firstIndex < lastIndex) {
			final Photo first = verticalPhotos.get(firstIndex);
			final Photo last = verticalPhotos.get(lastIndex);
			slides.add(new SlideVertical(first, last));
			firstIndex++;
			lastIndex--;
		}

		return slides;
	}

	public static List<Slide> getVerticalSlidesFirstSecond(final List<Photo> photos) {
		List<Photo> verticalPhotos = photos.stream().filter(Photo::isVertical)
				.sorted((photo0, photo1) -> Integer.compare(photo0.getTags().size(), photo1.getTags().size()))
				.collect(Collectors.toList());
		// First with second ...
		final List<Slide> slides = new ArrayList<>(verticalPhotos.size() / 2);

		for (int i = 0; i < verticalPhotos.size() - 1; i += 2) {
			final Photo first = verticalPhotos.get(i);
			final Photo second = verticalPhotos.get(i + 1);
			slides.add(new SlideVertical(first, second));
		}

		return slides;
	}

	public static List<Slide> solve(final List<Photo> photos) {
		List<Slide> slides = new LinkedList<>();
		slides.addAll(getHorizontalSlides(photos));
		slides.addAll(getVerticalSlidesFirstSecond(photos));
		return solveSlides(slides);
	}

	public static List<Slide> solveSlides(final List<Slide> slides) {
		final List<Slide> presentation = new ArrayList<>();
		final List<List<Slide>> order = splitByOrder(slides);

		for (final List<Slide> orderPhotos : order) {
			presentation.addAll(solveSameOrderQuick(orderPhotos));
		}

		return presentation;
	}

	public static List<Slide> solveSameOrderQuick(final List<Slide> slides) {
		final List<UniSlide> uniSlides = new ArrayList<>();
		final Map<Slide, List<UniSlide>> map = new HashMap<>();

		for (final Slide slide : slides) {
			map.put(slide, new ArrayList<>());

			for (final String tag : slide.getTags()) {
				final UniSlide us = new UniSlide(slide, tag);
				uniSlides.add(us);
				map.get(slide).add(us);
			}
		}

		uniSlides.sort((s1, s2) -> s1.tag.compareTo(s2.tag));

		while (uniSlides.size() > slides.size()) {
			System.out.println(uniSlides.size() + "\t" + slides.size());

			for (int i = 0; i < uniSlides.size(); i++) {
				uniSlides.get(i).score = 0;
			}

			for (int i = 0; i < uniSlides.size() - 1; i++) {
				final UniSlide us1 = uniSlides.get(i);
				final UniSlide us2 = uniSlides.get(i + 1);
				final int score = slideInterest(us1.slide, us2.slide);
				us1.score += score;
				us2.score += score;
			}

			map.forEach((slide, uniSlides2) -> {
				uniSlides2.sort((us1, us2) -> -Integer.compare(us1.score, us2.score));
				final int keep = (uniSlides2.size() + 1) / 2;

				while (uniSlides2.size() > keep) {
					uniSlides.remove(uniSlides2.remove(uniSlides2.size() - 1));
				}
			});
		}

		final List<Slide> presentation = new ArrayList<>();

		for (final UniSlide uniSlide : uniSlides) {
			presentation.add(uniSlide.slide);
		}

		return presentation;
	}

	public static List<Slide> solveSameOrder(final List<Slide> slides) {
		System.out.print("X");
		final Map<String, List<Slide>> tags1 = new HashMap<>();
		final Map<String, List<Slide>> tags2 = new HashMap<>();

		for (final Slide slide : slides) {
			for (final String tag1 : slide.getTags()) {
				tags1.computeIfAbsent(tag1, foo -> new ArrayList<>()).add(slide);

				for (final String tag2 : slide.getTags()) {
					if (tag2 == tag1) {
						continue;
					}

					final String tag12 = tag1.compareTo(tag2) > 0 ? tag1 + tag2 : tag2 + tag1;
					tags2.computeIfAbsent(tag12, foo -> new ArrayList<>()).add(slide);
				}
			}
		}

		// tags2.values().forEach(list -> System.out.println(list.size()));

		final List<List<Slide>> transitions = new ArrayList<>();

		tags2.forEach((tag, slides2) -> {
			slides2.forEach(slide1 -> {
				slides2.forEach(slide2 -> {
					if (slide1 == slide2) {
						return;
					}

					transitions.add(Arrays.asList(slide1, slide2));
				});
			});
		});

		transitions.sort((t1, t2) -> {
			return 0;
		});

		return Collections.emptyList();
	}

	public static List<List<Slide>> splitByOrder(final List<Slide> slides) {
		final List<List<Slide>> output = new ArrayList<>(100);

		for (int i = 0; i < 100; i++) {
			output.add(new ArrayList<>());
		}

		for (final Slide slide : slides) {
			output.get(slide.getTags().size() - 1).add(slide);
		}

		return output;
	}

	private static class UniSlide {
		public final Slide slide;
		public final String tag;
		public int score;

		public UniSlide(final Slide slide, final String tag) {
			this.slide = slide;
			this.tag = tag;
		}
	}

	public int score(final List<Slide> slides) {
		int result = 0;
		Slide lastSlide = null;

		for (final Slide slide : slides) {
			if (lastSlide == null) {
				lastSlide = slide;
				continue;
			}

			// Compute interest rate
			result += slideInterest(lastSlide, slide);
			lastSlide = slide;
		}

		return result;
	}
}