package org.example.generator.classes;

import org.example.annotation.Generatable;

@Generatable(subtypes = {ChildExample.class})
public class ParentExample {
    public ParentExample(EmptyEnum value) {
    }
}
