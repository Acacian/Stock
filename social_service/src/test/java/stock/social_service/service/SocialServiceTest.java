package stock.social_service.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import stock.social_service.client.NewsfeedServiceClient;
import stock.social_service.kafka.SocialEvent;
import stock.social_service.model.Comment;
import stock.social_service.model.Post;
import stock.social_service.repository.CommentRepository;
import stock.social_service.repository.FollowRepository;
import stock.social_service.repository.PostRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private NewsfeedServiceClient newsfeedServiceClient;

    @Test
    void testCreatePost() {
        Post post = new Post();
        post.setId(1L);
        post.setUserId(1L);
        post.setTitle("Test title");
        post.setContent("Test post");
        post.setStockId(10L);

        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post createdPost = socialService.createPost(1L, "Test title", "Test post", 10L);

        assertNotNull(createdPost);
        assertEquals("Test title", createdPost.getTitle());
        assertEquals("Test post", createdPost.getContent());
        verify(postRepository).save(any(Post.class));
        verify(newsfeedServiceClient).postCreated(argThat(event ->
                "POST_CREATED".equals(event.getType()) &&
                        Long.valueOf(1L).equals(event.getUserId()) &&
                        Long.valueOf(1L).equals(event.getTargetId())
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

        when(postRepository.findByUserId(userId, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(post1, post2)));

        Page<Post> posts = socialService.getPostsByUserId(userId, PageRequest.of(0, 10));

        assertEquals(2, posts.getContent().size());
        assertEquals("Test post 1", posts.getContent().get(0).getContent());
        assertEquals("Test post 2", posts.getContent().get(1).getContent());
    }

    @Test
    void testAddComment() {
        Long postId = 1L;
        Post post = new Post();
        post.setId(postId);
        post.setUserId(1L);
        post.setContent("Test post");

        Comment comment = new Comment();
        comment.setId(2L);
        comment.setUserId(2L);
        comment.setContent("Test comment");
        comment.setPost(post);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        Comment addedComment = socialService.addComment(2L, postId, "Test comment");

        assertNotNull(addedComment);
        assertEquals("Test comment", addedComment.getContent());
        verify(commentRepository).save(any(Comment.class));
        verify(newsfeedServiceClient).commentCreated(argThat(event ->
                "COMMENT_ADDED".equals(event.getType()) &&
                        Long.valueOf(2L).equals(event.getUserId()) &&
                        Long.valueOf(postId).equals(event.getTargetId()) &&
                        Long.valueOf(2L).equals(event.getAdditionalId())
        ));
    }

    @Test
    void testFollowUser() {
        Long followerId = 1L;
        Long followeeId = 2L;
        Post latestPost = new Post();
        latestPost.setId(3L);
        latestPost.setUserId(followeeId);

        when(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(false);
        when(postRepository.findTopByUserIdAndParentIsNullOrderByCreatedAtDesc(followeeId)).thenReturn(latestPost);

        socialService.follow(followerId, followeeId);

        verify(followRepository).save(any());
        verify(newsfeedServiceClient).userFollowed(argThat(event ->
                "USER_FOLLOWED".equals(event.getType()) &&
                        followerId.equals(event.getUserId()) &&
                        followeeId.equals(event.getTargetId())
        ));
        verify(newsfeedServiceClient).followerActivity(argThat(event ->
                "FOLLOWER_ACTIVITY".equals(event.getType()) &&
                        followerId.equals(event.getUserId()) &&
                        latestPost.getId().equals(event.getTargetId())
        ));
    }

    @Test
    void testUnfollowUser() {
        Long followerId = 1L;
        Long followeeId = 2L;

        socialService.unfollow(followerId, followeeId);

        verify(followRepository).deleteByFollowerIdAndFolloweeId(followerId, followeeId);
        verify(newsfeedServiceClient).userUnfollowed(argThat(event ->
                "USER_UNFOLLOWED".equals(event.getType()) &&
                        followerId.equals(event.getUserId()) &&
                        followeeId.equals(event.getTargetId())
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

        socialService.likePost(userId, postId);

        assertTrue(post.getLikes().contains(userId));
        verify(postRepository).save(post);
        verify(newsfeedServiceClient).postLiked(argThat(event ->
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

        socialService.unlikePost(userId, postId);

        assertFalse(post.getLikes().contains(userId));
        verify(postRepository).save(post);
        verify(newsfeedServiceClient).postUnliked(argThat(event ->
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
    void testGetPostsWithActivity() {
        Long userId = 1L;
        Post post1 = new Post();
        post1.setId(1L);
        post1.setUserId(userId);
        post1.setContent("Test post 1");
        post1.getComments().add(new Comment());
        post1.getComments().add(new Comment());
        post1.getLikes().add(2L);

        Post post2 = new Post();
        post2.setId(2L);
        post2.setUserId(userId);
        post2.setContent("Test post 2");
        post2.getComments().add(new Comment());
        post2.getLikes().add(2L);
        post2.getLikes().add(3L);

        when(postRepository.findByUserId(userId, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(post1, post2)));

        Page<Post> postsWithActivity = socialService.getPostsWithActivity(userId, PageRequest.of(0, 10));

        assertEquals(2, postsWithActivity.getContent().size());
        assertEquals(2, postsWithActivity.getContent().get(0).getCommentCount());
        assertEquals(1, postsWithActivity.getContent().get(0).getLikeCount());
        assertEquals(1, postsWithActivity.getContent().get(1).getCommentCount());
        assertEquals(2, postsWithActivity.getContent().get(1).getLikeCount());
    }
}
