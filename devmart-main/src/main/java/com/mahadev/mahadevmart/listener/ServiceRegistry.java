package com.mahadev.mahadevmart.listener;

import com.mahadev.mahadevmart.dao.CartDAO;
import com.mahadev.mahadevmart.dao.OrderDAO;
import com.mahadev.mahadevmart.dao.ProductDAO;
import com.mahadev.mahadevmart.dao.ReviewDAO;
import com.mahadev.mahadevmart.dao.UserDAO;
import com.mahadev.mahadevmart.dao.impl.JdbcCartDAO;
import com.mahadev.mahadevmart.dao.impl.JdbcOrderDAO;
import com.mahadev.mahadevmart.dao.impl.JdbcProductDAO;
import com.mahadev.mahadevmart.dao.impl.JdbcReviewDAO;
import com.mahadev.mahadevmart.dao.impl.JdbcUserDAO;
import com.mahadev.mahadevmart.chat.ChatProvider;
import com.mahadev.mahadevmart.chat.ChatService;
import com.mahadev.mahadevmart.chat.GeminiChatProvider;
import com.mahadev.mahadevmart.chat.MockChatProvider;
import com.mahadev.mahadevmart.service.CartService;
import com.mahadev.mahadevmart.service.OrderService;
import com.mahadev.mahadevmart.service.ProductService;
import com.mahadev.mahadevmart.service.ReviewService;
import com.mahadev.mahadevmart.service.UserService;

import javax.sql.DataSource;

/**
 * Small hand-rolled DI container: wires DAO implementations (Factory pattern,
 * Section 12) into services once at application startup and hands the same
 * instances to every servlet via the ServletContext. No framework needed at
 * this project's scale.
 */
public final class ServiceRegistry {

    public static final String ATTR = "mahadevmart.services";

    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final ChatService chatService;

    public ServiceRegistry(DataSource dataSource) {
        this(dataSource, loadChatConfig());
    }

    public ServiceRegistry(DataSource dataSource, java.util.Properties chatConfig) {
        UserDAO userDAO = new JdbcUserDAO(dataSource);
        ProductDAO productDAO = new JdbcProductDAO(dataSource);
        CartDAO cartDAO = new JdbcCartDAO(dataSource);
        OrderDAO orderDAO = new JdbcOrderDAO(dataSource);
        ReviewDAO reviewDAO = new JdbcReviewDAO(dataSource);

        this.userService = new UserService(userDAO);
        this.productService = new ProductService(productDAO);
        this.cartService = new CartService(cartDAO, productDAO);
        this.orderService = new OrderService(dataSource, orderDAO, productDAO, cartDAO);
        this.reviewService = new ReviewService(reviewDAO, orderDAO);

        // Section 17, Rule 2: implementation selected via config flag
        // ai.chatbot.provider=gemini|mock.
        String providerFlag = chatConfig.getProperty("ai.chatbot.provider", "mock");
        ChatProvider provider = "gemini".equalsIgnoreCase(providerFlag)
                ? new GeminiChatProvider(chatConfig.getProperty("ai.chatbot.apiKey", ""))
                : new MockChatProvider();
        this.chatService = new ChatService(provider);
    }

    private static java.util.Properties loadChatConfig() {
        java.util.Properties props = new java.util.Properties();
        try (var in = ServiceRegistry.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (java.io.IOException ignored) {
            // Defaults (mock provider) apply if config.properties is missing.
        }
        return props;
    }

    public UserService userService() {
        return userService;
    }

    public ProductService productService() {
        return productService;
    }

    public CartService cartService() {
        return cartService;
    }

    public OrderService orderService() {
        return orderService;
    }

    public ReviewService reviewService() {
        return reviewService;
    }

    public ChatService chatService() {
        return chatService;
    }
}
