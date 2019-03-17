package com.kazurayam.learninggroovy

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.learninggroovy.LearningClosure.Greeter
import com.kazurayam.learninggroovy.LearningClosure.Person
import com.kazurayam.learninggroovy.LearningClosure.Predicate
import com.kazurayam.learninggroovy.LearningClosure.Thing
import com.kazurayam.learninggroovy.LearningClosure.ThisIsEnclosedInInnerClass

import spock.lang.IgnoreRest
import spock.lang.Specification

/**
 * learning Apache Groovy document "Closures"
 * http://groovy-lang.org/closures.html
 * 
 * @author kazuayam
 *
 */
class LearningClosureSpec extends Specification {
    
    def test_smoke() {
        when:
            def aOpinion = true
            def anotherOpinion = false
        then:
            aOpinion == anotherOpinion
    }
    
    def closuresAsAnObject() {
        when:
            def listener = { e -> println "Clicked on $e.source "}
        then:
            listener instanceof Closure
        when:
            Closure callback = { println 'Done!'; return true; }
        then:
            callback()
            callback.call()
        when:
            Closure<Boolean> isTextFile = {
                File it -> it.name.endsWith('.txt')
            }
            Path a = Paths.get('./build/tmp/testOutput/LearningClusureSpec/a.txt')
            Files.createDirectories(a.getParent())
            a.toFile().text = 'a'
        then:
            isTextFile(a.toFile())
            isTextFile.call(a.toFile())
    }
    
    def callingAClosure() {
        when:
            def code = { 123 }
        then:
            code() == 123
            code.call() == 123
        //
        when:
            def isOdd = { int i -> i%2 != 0 }
        then:
            isOdd(3)
            ! isOdd.call(2)
        //
        when:
            def isEven = { it%2 == 0 }
        then:
            ! isEven(3)
            isEven.call(2)
    }
    
    def normalParameters() {
        when:
            def closureWithOneArg = { str -> str.toUpperCase() }
        then:
            closureWithOneArg('groovy') == 'GROOVY'
        //
        when:
            def closureWithOneArguAndExlicitType = { String str -> str.toUpperCase() }
        then:
            closureWithOneArg('groovy') == 'GROOVY'
        //
        when:
            def closureWithTwoArgs = { a,b -> a + b }
        then:
            closureWithTwoArgs(1,2) == 3
        //
        when:
            def closureWithTwoArgsAndExplicitTypes = { int a, int b -> a + b }
        then:
            closureWithTwoArgsAndExplicitTypes(1,2) == 3
        //
        when:
            def closureWithTwoArgsAndOptionalTypes = { a, int b -> a + b }
        then:
            closureWithTwoArgsAndOptionalTypes(1,2) == 3
        //
        when:
            def closureWithTwoArgsAndDefaultValue = { int a, int b=2 -> a + b }
        then:
            closureWithTwoArgsAndDefaultValue(1) == 3
    }
    
    def implicitParameter() {
        when:
            def greeting = { "Hello, $it!"}
        then:
            greeting('Patrick') == 'Hello, Patrick!'
    }
    
    def explicitEmtpyArgumentList() {
        when:
            def magicNumber = { -> 42 }
        then:
            magicNumber() == 42
        //
        when:
            magicNumber(11)
        then:
            thrown MissingMethodException
    }
    
    def varArgs() {
        when:
            def concat1 = { String... args -> args.join('')}
        then:
            concat1('abc', 'def') == 'abcdef'
        //
        /*
        when:
            def concat2 = { String[] args -> args.join('')}
        then:
            concant2('abc', 'def') == 'abcdef'
         */
        //
        when:
            def multiConcat = { int n, String... args ->
                args.join('')*n
            }
        then:
            multiConcat(2, 'abc', 'def') == 'abcdefabcdef'
    }
    
    def meaningOfThis() {
        expect:
            //new LearningClosure.ThisIsEnclosing().run()
            new LearningClosure.ThisIsEnclosedInInnerClass().run()
            new LearningClosure.ThisIsNestedClosures().run()
    }
    
    def callMethodsFromTheEnclosingClass() {
        when:
            def p = new LearningClosure.Person(name:'Janice', age:74)
        then:
            p.dump() == 'Janice is 74 years old'
    }
    
    def ownerOfAClosure() {
        expect:
            new LearningClosure.OwnerIsEnclosing().run()
            new LearningClosure.OwnerIsEnclosedInInnerClass().run()
            new LearningClosure.OwnerIsNestedClosures().run()
    }
    
    def delegateOfAClosure() {
        expect:
            new LearningClosure.DelegateOfEnclosing().run()
    }
    
    def delegateCanBeChanged() {
        setup:
            def p = new LearningClosure.Person(name:'Norman')
            def t = new LearningClosure.Thing(name:'Teapot')
            // Wow!
            def upperCaseName = { delegate.name.toUpperCase() }
        when:
            upperCaseName.delegate = p
        then:
            upperCaseName() == 'NORMAN'
        when:
            upperCaseName.delegate = t    // Wow!
        then:
            upperCaseName() == 'TEAPOT'   // Wow!
        
    }
    
    def delegationStrategy() {
        when:
            def p = new LearningClosure.Person(name:'Igor')
            def cl = { name.toUpperCase() }
            cl.delegate = p
        then:
            cl() == 'IGOR'
    }
    
    def delegationStrategy_ownerFirst() {
        when:
            def p = new LearningClosure.Person(name: 'Sarah')
            def t = new LearningClosure.Thing(name: 'Teapot')
            p.pretty.delegate = t
        then:
            p.toPrettyString()== 'My name is Sarah'
    }
    
    def delegationStragety_delegateFirst() {
        when:
            def p = new LearningClosure.Person(name:'Sarah')
            def t = new LearningClosure.Thing(name:'Teapot')
            p.pretty.delegate = t
            p.pretty.resolveStrategy = Closure.DELEGATE_FIRST
        then:
            p.toPrettyString()== 'My name is Teapot'
    }
    
    def closuresInGStrings() {
        when:
            def x = 1
            def gs = "x = ${x}"
        then:
            gs == 'x = 1'
        when:
            x = 2
        then:
            gs != 'x = 2'
    }
    
    def closuresInGString_enforceLazyEvaluation() {
        when:
            def x = 1
            def gs = "x = ${-> x}"
        then:
            gs == 'x = 1'
        when:
            x = 2
        then:
            gs == 'x = 2'
    }

    def closureInGString_mutation() {
        when:
            def sam = new LearningClosure.Person(name: 'Sam')
            def lucy = new LearningClosure.Person(name: 'Lucy')
            def p = sam
            def gs = "Name: ${p.getName()}"
        then:
            gs == 'Name: Sam'
        when:
            p = lucy
        then:
            gs == 'Name: Sam'
        when:
            sam.name = 'Lucy'
        then:
            gs == 'Name: Lucy'
    }
    
    
    def closureInGString_beExplicit() {
        when:
            def sam = new LearningClosure.Person(name: 'Sam')
            def lucy = new LearningClosure.Person(name: 'Lucy')
            def p = sam
            def gs = "Name: ${-> p.getName()}"
        then:
            gs == 'Name: Sam'
        when:
            p = 'Lucy'
        then:
            gs == 'Name: Lucy'
    }
    
    @IgnoreRest
    def assigningAClosureToASamType() {
        when:
            Predicate filter = { it.contains 'G'} as Predicate
        then:
            filter.accept('Groovy') == true
        when:
            Greeter greeter = { 'Groovy' } as Greeter
            greeter.greet()
    }
}
