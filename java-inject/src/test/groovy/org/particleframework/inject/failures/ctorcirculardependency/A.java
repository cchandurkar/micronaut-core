package org.particleframework.inject.failures.ctorcirculardependency;

import javax.inject.Singleton;

@Singleton
public class A {
    public A(C c) {}
}
