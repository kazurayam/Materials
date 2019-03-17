package com.kazurayam.learninggroovy

class LearningClosure {
    
    static class ThisIsEnclosing {
        boolean run() {
            def whatIsThisObject = { getThisObject() }
            assert whatIsThisObject() == this
            def whatIsThis = { this }
            assert whatIsThis() == this
            return true
        }
    }
    
    static class ThisIsEnclosedInInnerClass {
        class Inner {
            Closure cl = { this }
        }
        boolean run() {
            def inner = new Inner()
            assert inner.cl() == inner
            return true
        }
    }
    
    static class ThisIsNestedClosures {
        boolean run() {
            def nestedClosures = {
                def cl = { this }
                cl()
            }
            assert nestedClosures() == this
            return true
        }
    }
    
    static class Person {
        String name
        int age
        String getName() { name }
        String toString() { "$name is $age years old"}
        String dump() {
            def cl = {
                String msg = this.toString()
                println msg
                msg
            }
            cl()
        }
        Closure pretty = { "My name is $name" }
        String toPrettyString() {
            pretty()
        }
    }
    
    static class OwnerIsEnclosing {
        boolean run() {
            def whatIsOwnerMethod = { getOwner() }
            assert whatIsOwnerMethod() == this
            def whatIsOwner = { owner }
            assert whatIsOwner() == this
            return true
        }
    }
    
    static class OwnerIsEnclosedInInnerClass {
        class Inner {
            Closure cl = { owner }
        }
        boolean run() {
            def inner = new Inner()
            assert inner.cl() == inner
            return true
        }
    }
    
    static class OwnerIsNestedClosures {
        boolean run() {
            def nestedClosures = {
                def cl = { owner }
                cl()
            }
            assert nestedClosures() == nestedClosures
            return true
        }
    }
    
    static class DelegateOfEnclosing {
        boolean run() {
            def cl = { getDelegate() }
            def cl2 = { delegate }
            assert cl() == cl2()
            assert cl() == this
            def enclosed = {
                { -> delegate }.call()
            }
            assert enclosed() == enclosed
            return true
        }
    }
    
    static class Thing {
        String name
    }
    
    static interface Predicate<T> {
        boolean accept(T obj)
    }
    
    /**
     * SAM : Abstract class with Single Abstract Method
     */
    static abstract class Greeter {
        abstract String getName()
        void greet() {
            println "Hello, $name"
        }
    }
}
