package com.moneynote.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;

/**
 * classpath 资源读取工具：把「资源文件 → 文本」这段通用逻辑收在一处。
 * 提示词、模板、内置页面等任何外置文本都用它读，业务包内不再各自造轮子。
 *
 * <p>两条约定：
 * <ul>
 *   <li><b>编码固定默认 UTF-8</b>——中文提示词/模板用平台默认编码读会乱码，
 *       且在不同部署环境表现不一致。</li>
 *   <li><b>缺失即失败</b>——资源缺失是打包/部署缺陷，直接抛
 *       {@link IllegalStateException}，不静默返回空串让上层带病运行。</li>
 * </ul>
 *
 * <p>顺带兼容 Spring 风格的 {@code classpath:} 前缀，两种写法等价。
 */
public final class ClasspathResources {

    private static final String CLASSPATH_PREFIX = "classpath:";

    private ClasspathResources() {
    }

    /** 按 UTF-8 读取 classpath 资源全文 */
    public static String readText(String location) {
        return readText(location, StandardCharsets.UTF_8);
    }

    /** 按指定字符集读取 classpath 资源全文 */
    public static String readText(String location, Charset charset) {
        try (InputStream in = new ClassPathResource(normalize(location)).getInputStream()) {
            return new String(in.readAllBytes(), charset);
        } catch (IOException e) {
            throw new IllegalStateException("classpath 资源读取失败: " + location, e);
        }
    }

    /** 去掉 {@code classpath:} 前缀与开头的 {@code /}，ClassPathResource 只认裸路径 */
    private static String normalize(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("classpath 资源路径不能为空");
        }
        String path = location.startsWith(CLASSPATH_PREFIX)
                ? location.substring(CLASSPATH_PREFIX.length())
                : location;
        return path.startsWith("/") ? path.substring(1) : path;
    }

}
