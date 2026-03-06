package stock.social_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

import stock.social_service.model.Post;
import stock.social_service.kafka.SocialEvent;
import stock.social_service.model.Comment;
import stock.social_service.repository.PostRepository;
import stock.social_service.repository.CommentRepository;
import stock.social_service.repository.FollowRepository;

import java.util.Optional;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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

    @MockBean
    private KafkaTemplate<String, SocialEvent> kafkaTemplate;

    @BeforeEach
    void setup() {
        // Setup mock behavior if needed
    }

    @Test
    void testCreatePost() {
        Post post = new Post();
        post.setId(1L);
        post.setUserId(1L);
        post.setContent("Test post");

        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post createdPost = socialService.createPost(post.getUserId(), post.getContent());

        assertNotNull(createdPost);
        assertEquals("Test post", createdPost.getContent());
        verify(postRepository).save(any(Post.class));
        verify(kafkaTemplate).send(eq("social-events"), argThat(event -> 
            "POST_CREATED".equals(event.getType()) &&
            post.getUserId().equals(event.getUserId()) &&
            post.getId().equals(event.getTargetId())
        ));
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
        verify(kafkaTemplate).send(eq("social-events"), argThat(event -> 
            "COMMENT_ADDED".equals(event.getType()) &&
            comment.getUserId().equals(event.getUserId()) &&
            postId.equals(event.getTargetId())
        ));
    }

    @Test
    void testFollowUser() {
        Long followerId = 1L;
        Long followedId = 2L;

        socialService.followUser(followerId, followedId);

        verify(followRepository).save(any());
        verify(kafkaTemplate).send(eq("social-events"), argThat(event -> 
            "USER_FOLLOWED".equals(event.getType()) &&
            followerId.equals(event.getUserId()) &&
            followedId.equals(event.getTargetId())
        ));
    }

    @Test
    void testUnfollowUser() {
        Long followerId = 1L;
        Long followedId = 2L;

        socialService.unfollowUser(followerId, followedId);

        verify(followRepository).deleteByFollowerIdAndFollowedId(followerId, followedId);
        verify(kafkaTemplate).send(eq("social-events"), argThat(event -> 
            "USER_UNFOLLOWED".equals(event.getType()) &&
            followerId.equals(event.getUserId()) &&
            followedId.equals(event.getTargetId())
        ));
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
        verify(kafkaTemplate).send(eq("social-events"), argThat(event -> 
            "POST_LIKED".equals(event.getType()) &&
            userId.equals(event.getUserId()) &&
            postId.equals(event.getTargetId())
        ));
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
        verify(kafkaTemplate).send(eq("social-events"), argThat(event -> 
            "POST_UNLIKED".equals(event.getType()) &&
            userId.equals(event.getUserId()) &&
            postId.equals(event.getTargetId())
        ));
    }

    @Test
    void testGetPostById() {
        Long postId = 1L;
        Post post = new Post();
        post.setId(postId);
        post.setContent("Test post");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        Post retrievedPost = socialService.getPostById(postId);

        assertNotNull(retrievedPost);
        assertEquals(postId, retrievedPost.getId());
        assertEquals("Test post", retrievedPost.getContent());
    }

    @Test
    void testCreateFollowerActivity() {
        Long followerId = 1L;
        Long followedId = 2L;
        Post latestPost = new Post();
        latestPost.setId(3L);
        latestPost.setUserId(followedId);

        when(postRepository.findTopByUserIdOrderByCreatedAtDesc(followedId)).thenReturn(latestPost);

        socialService.createFollowerActivity(followerId, followedId);

        verify(kafkaTemplate).send(eq("social-events"), argThat(event -> 
            "FOLLOWER_ACTIVITY".equals(event.getType()) &&
            followerId.equals(event.getUserId()) &&
            latestPost.getId().equals(event.getTargetId())
        ));
    }

    @Test
    void testGetPostsWithActivity() {
        Long userId = 1L;
        Post post1 = new Post();
        post1.setId(1L);
        post1.setUserId(userId);
        post1.setContent("Test post 1");
        Post post2 = new Post();
        post2.setId(2L);
        post2.setUserId(userId);
        post2.setContent("Test post 2");

        when(postRepository.findByUserId(userId)).thenReturn(Arrays.asList(post1, post2));
        when(commentRepository.countByPostId(1L)).thenReturn(2L);
        when(commentRepository.countByPostId(2L)).thenReturn(1L);

        List<Post> postsWithActivity = socialService.getPostsWithActivity(userId);

        assertEquals(2, postsWithActivity.size());
        assertEquals(2, postsWithActivity.get(0).getCommentCount());
        assertEquals(1, postsWithActivity.get(1).getCommentCount());
        verify(postRepository).findByUserId(userId);
        verify(commentRepository, times(2)).countByPostId(anyLong());
    }
}