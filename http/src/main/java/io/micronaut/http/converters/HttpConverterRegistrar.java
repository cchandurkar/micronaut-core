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
package io.micronaut.http.converters;

import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.convert.MutableConversionService;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.core.io.Readable;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.ResourceResolver;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.net.URL;
import java.util.Optional;

/**
 * Converter registrar for HTTP classes.
 *
 * @author graemerocher
 * @since 2.0
 */
@Prototype
public class HttpConverterRegistrar implements TypeConverterRegistrar {

    private final BeanProvider<ResourceResolver> resourceResolver;

    /**
     * Default constructor.
     *
     * @param resourceResolver The resource resolver
     */
    @Inject
    protected HttpConverterRegistrar(BeanProvider<ResourceResolver> resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    /**
     * The constructor.
     *
     * @param resourceResolver The resource resolver
     * @deprecated Replaced by {@link #HttpConverterRegistrar(BeanProvider)}.
     */
    @Deprecated(forRemoval = true)
    protected HttpConverterRegistrar(Provider<ResourceResolver> resourceResolver) {
        this.resourceResolver = new BeanProvider<ResourceResolver>() {
            @Override
            public ResourceResolver get() {
                return resourceResolver.get();
            }
        };
    }

    @Override
    public void register(MutableConversionService conversionService) {
        conversionService.addConverter(
                CharSequence.class,
                Readable.class,
                (object, targetType, context) -> {
                    String pathStr = object.toString();
                    Optional<ResourceLoader> supportingLoader = resourceResolver.get().getSupportingLoader(pathStr);
                    if (supportingLoader.isEmpty()) {
                        context.reject(pathStr, new ConfigurationException(
                                "No supported resource loader for path [" + pathStr + "]. Prefix the path with a supported prefix such as 'classpath:' or 'file:'"
                        ));
                        return Optional.empty();
                    } else {
                        final Optional<URL> resource = resourceResolver.get().getResource(pathStr);
                        if (resource.isPresent()) {
                            return Optional.of(Readable.of(resource.get()));
                        } else {
                            context.reject(object, new ConfigurationException("No resource exists for value: " + object));
                            return Optional.empty();
                        }
                    }

                }
        );
    }
}
