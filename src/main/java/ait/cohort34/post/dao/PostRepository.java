package ait.cohort34.post.dao;

import ait.cohort34.post.model.Post;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;


public interface PostRepository extends CrudRepository<Post,String> {
    Stream<Post> findPostsByAuthor(String author);
    @Query("{ 'tags' : { $in: ?0 } }")
    Stream<Post> findByTagsIn(Set<String> tags);
    @Query("{ 'dateCreated' : { $gte: ?0, $lte: ?1 } }")
    Stream<Post> findByDateFromGreaterThanEqualAndDateToLessThenEqual(LocalDate startDate, LocalDate endDate);
}
