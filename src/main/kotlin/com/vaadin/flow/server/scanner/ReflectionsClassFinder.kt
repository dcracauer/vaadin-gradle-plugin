/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.server.scanner

import com.vaadin.flow.server.frontend.scanner.ClassFinder
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import java.lang.annotation.Repeatable
import java.lang.reflect.AnnotatedElement
import java.net.URL
import java.net.URLClassLoader
import java.util.*

/**
 * A class finder using org.reflections.
 *
 * @param urls
 * @since 2.0
 */
class ReflectionsClassFinder(vararg urls: URL?) : ClassFinder {
    @Transient
    private val classLoader: ClassLoader

    @Transient
    private val reflections: Reflections

    init {
        classLoader = URLClassLoader(urls, null) // NOSONAR
        reflections = Reflections(
                ConfigurationBuilder().addClassLoader(classLoader)
                        .setExpandSuperTypes(false).addUrls(*urls))
    }


    override fun getAnnotatedClasses(
            clazz: Class<out Annotation?>): Set<Class<*>> {
        val classes: MutableSet<Class<*>> = HashSet()
        classes.addAll(reflections.getTypesAnnotatedWith(clazz, true))
        classes.addAll(getAnnotatedByRepeatedAnnotation(clazz))
        return classes
    }

    private fun getAnnotatedByRepeatedAnnotation(
            annotationClass: AnnotatedElement?): Set<Class<*>> {
        if (annotationClass == null) return emptySet()
        val repeatableAnnotation = annotationClass
                .getAnnotation(Repeatable::class.java)
        return if (repeatableAnnotation != null) {
            val value = repeatableAnnotation.value::class.java as Class<Annotation>
            reflections.getTypesAnnotatedWith(
                    value, true)
        } else emptySet()
    }

    override fun getResource(name: String?): URL? {
        return classLoader.getResource(name)
    }

    @Throws(ClassNotFoundException::class)
    override fun <T> loadClass(name: String?): Class<T>? {
        return classLoader.loadClass(name) as Class<T>
    }

    override fun <T> getSubTypesOf(type: Class<T>?): Set<Class<out T>>? {
        return reflections.getSubTypesOf(type)
    }

    override fun getClassLoader(): ClassLoader {
        return classLoader
    }


}