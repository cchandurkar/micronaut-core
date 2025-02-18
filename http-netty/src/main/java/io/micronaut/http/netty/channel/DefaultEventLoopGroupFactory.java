/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.http.netty.channel;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.netty.configuration.NettyGlobalConfiguration;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.unix.ServerDomainSocketChannel;
import io.netty.util.ResourceLeakDetector;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * The default factory for {@link EventLoopGroup} instances.
 *
 * @author graemerocher
 * @since 1.0
 */
@Primary
@Singleton
@BootstrapContextCompatible
public class DefaultEventLoopGroupFactory implements EventLoopGroupFactory {

    private final EventLoopGroupFactory nativeFactory;
    private final EventLoopGroupFactory defaultFactory;

    /**
     * Default constructor.
     * @param nioEventLoopGroupFactory The NIO factory
     * @param nativeFactory The native factory if available
     */
    public DefaultEventLoopGroupFactory(
            NioEventLoopGroupFactory nioEventLoopGroupFactory,
            @Nullable @Named(EventLoopGroupFactory.NATIVE) EventLoopGroupFactory nativeFactory) {
        this(nioEventLoopGroupFactory, nativeFactory, null);
    }

    /**
     * Default constructor.
     * @param nioEventLoopGroupFactory The NIO factory
     * @param nativeFactory The native factory if available
     * @param nettyGlobalConfiguration The netty global configuration
     */
    @Inject
    public DefaultEventLoopGroupFactory(
            NioEventLoopGroupFactory nioEventLoopGroupFactory,
            @Nullable @Named(EventLoopGroupFactory.NATIVE) EventLoopGroupFactory nativeFactory,
            @Nullable NettyGlobalConfiguration nettyGlobalConfiguration) {
        this.defaultFactory = nioEventLoopGroupFactory;
        this.nativeFactory = nativeFactory != null ? nativeFactory : defaultFactory;
        if (nettyGlobalConfiguration != null && nettyGlobalConfiguration.getResourceLeakDetectorLevel() != null) {
            ResourceLeakDetector.setLevel(nettyGlobalConfiguration.getResourceLeakDetectorLevel());
        } else if (ResourceLeakDetector.getLevel() == ResourceLeakDetector.Level.SIMPLE &&
            System.getProperty("io.netty.leakDetectionLevel") == null &&
            System.getProperty("io.netty.leakDetection.level") == null) {
            // disable leak detection for performance if it's not explicitly enabled in a system
            // property, via config, or via setLevel
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        }

        if (System.getProperty("io.netty.allocator.maxOrder") == null) {
            System.setProperty("io.netty.allocator.maxOrder", "3");
        }
    }

    @Override
    public EventLoopGroup createEventLoopGroup(EventLoopGroupConfiguration configuration, ThreadFactory threadFactory) {
        ArgumentUtils.requireNonNull("configuration", configuration);
        ArgumentUtils.requireNonNull("threadFactory", threadFactory);

        return getFactory(configuration).createEventLoopGroup(configuration, threadFactory);
    }

    @Override
    public EventLoopGroup createEventLoopGroup(int threads, Executor executor, @Nullable Integer ioRatio) {
        return nativeFactory.createEventLoopGroup(threads, executor, ioRatio);
    }

    @Override
    public EventLoopGroup createEventLoopGroup(int threads, @Nullable ThreadFactory threadFactory, @Nullable Integer ioRatio) {
        return nativeFactory.createEventLoopGroup(threads, threadFactory, ioRatio);
    }

    @Override
    public Class<? extends ServerSocketChannel> serverSocketChannelClass() {
        return nativeFactory.serverSocketChannelClass();
    }

    @Override
    public Class<? extends ServerDomainSocketChannel> domainServerSocketChannelClass() throws UnsupportedOperationException {
        return nativeFactory.domainServerSocketChannelClass();
    }

    @Override
    public Class<? extends Channel> channelClass(NettyChannelType type) throws UnsupportedOperationException {
        return nativeFactory.channelClass(type);
    }

    @NonNull
    @Override
    public Class<? extends ServerSocketChannel> serverSocketChannelClass(EventLoopGroupConfiguration configuration) {
        return getFactory(configuration).serverSocketChannelClass(configuration);
    }

    @NonNull
    @Override
    public Class<? extends ServerDomainSocketChannel> domainServerSocketChannelClass(EventLoopGroupConfiguration configuration) {
        return getFactory(configuration).domainServerSocketChannelClass(configuration);
    }

    @Override
    public Class<? extends Channel> channelClass(NettyChannelType type, @Nullable EventLoopGroupConfiguration configuration) {
        return getFactory(configuration).channelClass(type, configuration);
    }

    @Override
    public ServerSocketChannel serverSocketChannelInstance(EventLoopGroupConfiguration configuration) {
        return getFactory(configuration).serverSocketChannelInstance(configuration);
    }

    @Override
    public ServerChannel domainServerSocketChannelInstance(@Nullable EventLoopGroupConfiguration configuration) {
        return getFactory(configuration).domainServerSocketChannelInstance(configuration);
    }

    @Override
    public Channel channelInstance(NettyChannelType type, @Nullable EventLoopGroupConfiguration configuration) {
        return getFactory(configuration).channelInstance(type, configuration);
    }

    @NonNull
    @Override
    public Class<? extends SocketChannel> clientSocketChannelClass(@Nullable EventLoopGroupConfiguration configuration) {
        return getFactory(configuration).clientSocketChannelClass(configuration);
    }

    private EventLoopGroupFactory getFactory(@Nullable EventLoopGroupConfiguration configuration) {
        if (configuration != null && configuration.isPreferNativeTransport()) {
            return this.nativeFactory;
        } else {
            return this.defaultFactory;
        }
    }

    @NonNull
    @Override
    public SocketChannel clientSocketChannelInstance(@Nullable EventLoopGroupConfiguration configuration) {
        return getFactory(configuration).clientSocketChannelInstance(configuration);
    }

}
