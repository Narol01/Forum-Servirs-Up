package ait.cohort34.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NewPostDto {
    private String title;
    private String content;
    private Set<String> tags;
}
