/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.docs.client.versioning

// tag::imports[]
import io.micronaut.core.version.annotation.Version
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import reactor.core.publisher.Mono
// end::imports[]

// tag::clazz[]
@Client("/hello")
@Version("1") // <1>
interface HelloClient {

    @Get("/greeting/{name}")
    fun sayHello(name : String) : String

    @Version("2")
    @Get("/greeting/{name}")
    fun sayHelloTwo(name : String) : Mono<String>  // <2>
}
// end::clazz[]
