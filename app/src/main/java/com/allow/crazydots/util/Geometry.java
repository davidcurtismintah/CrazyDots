package com.allow.crazydots.util;


public class Geometry {

    public static class Point {
        public final float x, y, z;

        public Point(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Point translateY(float distance) {
            return new Point(x, y + distance, z);
        }

        public Point translate(Vector vector) {
            return new Point(
                    x + vector.x,
                    y + vector.y,
                    z + vector.z);
        }

    }

    public static class DotPoint {
        public final float x, y, z;
        public boolean dotPressed;
        public boolean upDrawn;
        public boolean downDrawn;
        public boolean leftDrawn;
        public boolean rightDrawn;

        public DotPoint(float x, float y, float z, boolean dotPressed) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dotPressed = dotPressed;
        }

        public Point translateY(float distance) {
            return new Point(x, y + distance, z);
        }

        public Point translate(Vector vector) {
            return new Point(
                    x + vector.x,
                    y + vector.y,
                    z + vector.z);
        }

        public float distance(DotPoint dotPoint){
            return (float) Math.sqrt(
                    (dotPoint.x - this.x)*(dotPoint.x - this.x)
                            + (dotPoint.y - this.y)*(dotPoint.y - this.y)
                            + (dotPoint.z- this.z)*(dotPoint.z - this.z));
        }
    }

    public static class Circle {
        public final Point center;
        public final float radius;

        public Circle(Point center, float radius) {
            this.center = center;
            this.radius = radius;
        }

        public Circle scale(float scale) {
            return new Circle(center, radius * scale);
        }
    }

    public static class Rectangle{
        public final float width, height;

        public Rectangle(float width, float height) {
            this.width = width;
            this.height = height;
        }
    }

    public static class Cylinder {
        public final Point center;
        public final float radius;
        public final float height;

        public Cylinder(Point center, float radius, float height) {
            this.center = center;
            this.radius = radius;
            this.height = height;
        }
    }

    public static class Ray {
        public final Point point;
        public final Vector vector;

        public Ray(Point point, Vector vector) {
            this.point = point;
            this.vector = vector;
        }
    }

    public static class Vector {
        public final float x, y, z;

        public Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float length() {
            return (float) Math.sqrt(
                    x * x
                            + y * y
                            + z * z);
        }

        public Vector crossProduct( Vector other) {
            return new Vector(
                    (y * other.z) - (z * other.y),
                    (z * other.x) - (x * other.z),
                    (x * other.y) - (y * other.x));
        }

        public float dotProduct( Vector other) {
            return x * other.x
                    + y * other.y
                    + z * other.z;
        }

        public Vector scale( float f) {
            return new Vector(
                    x * f,
                    y * f,
                    z * f);
        }
    }


    public static class Sphere {
        public final Point center;
        public final float radius;

        public Sphere( Point center, float radius) {
            this.center = center;
            this.radius = radius;
        }
    }

    public static class Plane {
        public final Point point;
        public final Vector normal;
        public Plane( Point point, Vector normal) {
            this.point = point;
            this.normal = normal;
        }
    }

    public static Vector vectorBetween( Point from, Point to) {
        return new Vector(
                to.x - from.x,
                to.y - from.y,
                to.z - from.z);
    }

    public static boolean intersects(Sphere sphere, Ray ray) {
        return distanceBetween(sphere.center, ray) < sphere.radius;
    }

    public static float distanceBetween(Point point, Ray ray) {
        Vector p1ToPoint = vectorBetween(ray.point, point);
        Vector p2ToPoint = vectorBetween(ray.point.translate(ray.vector), point);
        float areaOfTriangleTimesTwo = p1ToPoint.crossProduct(p2ToPoint).length();
        float lengthOfBase = ray.vector.length();
        float distanceFromPointToRay = areaOfTriangleTimesTwo / lengthOfBase;
        return distanceFromPointToRay;
    }

    public static Point intersectionPoint(Ray ray, Plane plane) {
        Vector rayToPlaneVector = vectorBetween(ray.point, plane.point);
        float scaleFactor = rayToPlaneVector.dotProduct(plane.normal)
                / ray.vector.dotProduct(plane.normal);
        Point intersectionPoint = ray.point.translate(ray.vector.scale(scaleFactor));
        return intersectionPoint;
    }

    public static class LineSegment{
        public final Point point1;
        public final Point point2;

        public LineSegment(Point point1, Point point2) {
            this.point1 = point1;
            this.point2 = point2;
        }
    }
}
