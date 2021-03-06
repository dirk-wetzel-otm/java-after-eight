package org.codefx.java_after_eight.recommendation;

import org.codefx.java_after_eight.article.Article;
import org.codefx.java_after_eight.genealogy.Relation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

// Don't judge me for the name - recommend a better one (see what I did there?)
public class Recommender {

	public Stream<Recommendation> recommend(Stream<Relation> relations, int perArticle) {
		if (perArticle < 1)
			throw new IllegalArgumentException(
					"Number of recommendations per article must be greater zero: " + perArticle);

		Comparator<Relation> byDecreasingScoreThenByArticle =
				comparing(Relation::score)
						.reversed()
						.thenComparing((Relation relation) -> relation.article2().title().text());

		Map<Article, List<Relation>> byArticle = relations.collect(groupingBy(Relation::article1));
		return byArticle
				.entrySet().stream()
				.map(articleWithRelations -> Recommendation.from(
						articleWithRelations.getKey(),
						articleWithRelations.getValue().stream()
								.sorted(byDecreasingScoreThenByArticle)
								.map(Relation::article2),
						perArticle))
				.sorted(comparing(recommendation -> recommendation.article().title().text()));

	}

}
