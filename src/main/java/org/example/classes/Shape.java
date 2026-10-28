package org.example.classes;

import org.example.annotation.Generatable;

@Generatable(subtypes = {Triangle.class, Rectangle.class})
public interface Shape {
    double getArea();
    double getPerimeter();
}