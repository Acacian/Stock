package stock.social_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import stock.social_service.model.Post;
import stock.social_service.model.Comment;
import stock.social_service.repository.PostRepository;
import stock.social_service.repository.CommentRepository;
import stock.social_service.repository.FollowRepository;

import java.util.Optional;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class SocialServiceTest {

    @Autowired
    private SocialService socialService;

    @MockBean
    private PostRepository postRepository;

    @MockBean
    private CommentRepository commentRepository;

    @MockBean
    private FollowRepository followRepository;

    @BeforeEach
    void setup() {
        // Setup mock behavior if needed
    }

    @Test
    void testCreatePost() {
        Post post = new Post();
        post.setUserId(1L);
        post.setContent("Test post");

        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post createdPost = socialService.createPost(post.getUserId(), post.getContent());

        assertNotNull(createdPost);
        assertEquals("Test post", createdPost.getContent());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void testGetPostsByUserId() {
        Long userId = 1L;
        Post post1 = new Post();
        post1.setUserId(userId);
        post1.setContent("Test post 1");
        Post post2 = new Post();
        post2.setUserId(userId);
        post2.setContent("Test post 2");

        when(postRepository.findByUserId(userId)).thenReturn(Arrays.asList(post1, post2));

        List<Post> posts = socialService.getPostsByUserId(userId);

        assertEquals(2, posts.size());
        assertEquals("Test post 1", posts.get(0).getContent());
        assertEquals("Test post 2", posts.get(1).getContent());
    }

    @Test
    void testAddComment() {
        Long postId = 1L;
        Post post = new Post();
        post.setId(postId);
        post.setUserId(1L);
        post.setContent("Test post");

        Comment comment = new Comment();
        comment.setUserId(2L);
        comment.setContent("Test comment");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment addedComment = socialService.addComment(postId, comment.getUserId(), comment.getContent());

        assertNotNull(addedComment);
        assertEquals("Test comment", addedComment.getContent());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void testFollowUser() {
        Long followerId = 1L;
        Long followedId = 2L;

        socialService.followUser(followerId, followedId);

        verify(followRepository).save(any());
    }

    @Test
    void testUnfollowUser() {
        Long followerId = 1L;
        Long followedId = 2L;

        socialService.unfollowUser(followerId, followedId);

        verify(followRepository).deleteByFollowerIdAndFollowedId(followerId, followedId);
    }

    @Test
    void testLikePost() {
        Long postId = 1L;
        Long userId = 1L;
        Post post = new Post();
        post.setId(postId);
        post.setUserId(2L);
        post.setContent("Test post");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        socialService.likePost(postId, userId);

        assertTrue(post.getLikes().contains(userId));
        verify(postRepository).save(post);
    }

    @Test
    void testUnlikePost() {
        Long postId = 1L;
        Long userId = 1L;
        Post post = new Post();
        post.setId(postId);
        post.setUserId(2L);
        post.setContent("Test post");
        post.getLikes().add(userId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        socialService.unlikePost(postId, userId);

        assertFalse(post.getLikes().contains(userId));
        verify(postRepository).save(post);
    }
}