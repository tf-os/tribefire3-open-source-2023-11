// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.gwt.processdesigner.client.vector;

public class Complex extends Object {

    public double x,y;
    
    /**
        Constructs the complex number z = u + i*v
        @param u Real part
        @param v Imaginary part
    */
    public Complex(double u,double v) {
        x=u;
        y=v;
    }
    
    /**
        Real part of this Complex number 
        (the x-coordinate in rectangular coordinates).
        @return Re[z] where z is this Complex number.
    */
    public double real() {
        return x;
    }
    
    /**
        Imaginary part of this Complex number 
        (the y-coordinate in rectangular coordinates).
        @return Im[z] where z is this Complex number.
    */
    public double imag() {
        return y;
    }
    
    /**
        Modulus of this Complex number
        (the distance from the origin in polar coordinates).
        @return |z| where z is this Complex number.
    */
    public double mod() {
        if (x!=0 || y!=0) {
            return Math.sqrt(x*x+y*y);
        } else {
            return 0d;
        }
    }
    
    /**
        Argument of this Complex number 
        (the angle in radians with the x-axis in polar coordinates).
        @return arg(z) where z is this Complex number.
    */
    public double arg() {
        return Math.atan2(y,x);
    }
    
    /**
        Complex conjugate of this Complex number
        (the conjugate of x+i*y is x-i*y).
        @return z-bar where z is this Complex number.
    */
    public Complex conj() {
        return new Complex(x,-y);
    }
    
    /**
        Addition of Complex numbers (doesn't change this Complex number).
        <br>(x+i*y) + (s+i*t) = (x+s)+i*(y+t).
        @param w is the number to add.
        @return z+w where z is this Complex number.
    */
    public Complex plus(Complex w) {
        return new Complex(x+w.real(),y+w.imag());
    }
    
    /**
        Subtraction of Complex numbers (doesn't change this Complex number).
        <br>(x+i*y) - (s+i*t) = (x-s)+i*(y-t).
        @param w is the number to subtract.
        @return z-w where z is this Complex number.
    */
    public Complex minus(Complex w) {
        return new Complex(x-w.real(),y-w.imag());
    }
    
    /**
        Complex multiplication (doesn't change this Complex number).
        @param w is the number to multiply by.
        @return z*w where z is this Complex number.
    */
    public Complex times(Complex w) {
        return new Complex(x*w.real()-y*w.imag(),x*w.imag()+y*w.real());
    }
    
    public Complex times(double s){
    	return new Complex(x*s, y*s);
    }
    
    /**
        Division of Complex numbers (doesn't change this Complex number).
        <br>(x+i*y)/(s+i*t) = ((x*s+y*t) + i*(y*s-y*t)) / (s^2+t^2)
        @param w is the number to divide by
        @return new Complex number z/w where z is this Complex number  
    */
    public Complex div(Complex w) {
        double den=Math.pow(w.mod(),2);
        return new Complex((x*w.real()+y*w.imag())/den,(y*w.real()-x*w.imag())/den);
    }
    
    public Complex div(double s){
    	return new Complex(x/s, y/s);
    }
    
    /**
        Complex exponential (doesn't change this Complex number).
        @return exp(z) where z is this Complex number.
    */
    public Complex exp() {
        return new Complex(Math.exp(x)*Math.cos(y),Math.exp(x)*Math.sin(y));
    }
    
    /**
        Principal branch of the Complex logarithm of this Complex number.
        (doesn't change this Complex number).
        The principal branch is the branch with -pi < arg <= pi.
        @return log(z) where z is this Complex number.
    */
    public Complex log() {
        return new Complex(Math.log(this.mod()),this.arg());
    }
    
    /**
        Complex square root (doesn't change this complex number).
        Computes the principal branch of the square root, which 
        is the value with 0 <= arg < pi.
        @return sqrt(z) where z is this Complex number.
    */
    public Complex sqrt() {
        double r=Math.sqrt(this.mod());
        double theta=this.arg()/2;
        return new Complex(r*Math.cos(theta),r*Math.sin(theta));
    }
    
    // Real cosh function (used to compute complex trig functions)
    private double cosh(double theta) {
        return (Math.exp(theta)+Math.exp(-theta))/2;
    }
    
    // Real sinh function (used to compute complex trig functions)
    private double sinh(double theta) {
        return (Math.exp(theta)-Math.exp(-theta))/2;
    }
    
    /**
        Sine of this Complex number (doesn't change this Complex number).
        <br>sin(z) = (exp(i*z)-exp(-i*z))/(2*i).
        @return sin(z) where z is this Complex number.
    */
    public Complex sin() {
        return new Complex(cosh(y)*Math.sin(x),sinh(y)*Math.cos(x));
    }
    
    /**
        Cosine of this Complex number (doesn't change this Complex number).
        <br>cos(z) = (exp(i*z)+exp(-i*z))/ 2.
        @return cos(z) where z is this Complex number.
    */
    public Complex cos() {
        return new Complex(cosh(y)*Math.cos(x),-sinh(y)*Math.sin(x));
    }
    
    /**
        Hyperbolic sine of this Complex number 
        (doesn't change this Complex number).
        <br>sinh(z) = (exp(z)-exp(-z))/2.
        @return sinh(z) where z is this Complex number.
    */
    public Complex sinh() {
        return new Complex(sinh(x)*Math.cos(y),cosh(x)*Math.sin(y));
    }
    
    /**
        Hyperbolic cosine of this Complex number 
        (doesn't change this Complex number).
        <br>cosh(z) = (exp(z) + exp(-z)) / 2.
        @return cosh(z) where z is this Complex number.
    */
    public Complex cosh() {
        return new Complex(cosh(x)*Math.cos(y),sinh(x)*Math.sin(y));
    }
    
    /**
        Tangent of this Complex number (doesn't change this Complex number).
        <br>tan(z) = sin(z)/cos(z).
        @return tan(z) where z is this Complex number.
    */
    public Complex tan() {
        return (this.sin()).div(this.cos());
    }
    
    /**
        Negative of this complex number (chs stands for change sign). 
        This produces a new Complex number and doesn't change 
        this Complex number.
        <br>-(x+i*y) = -x-i*y.
        @return -z where z is this Complex number.
    */
    public Complex chs() {
        return new Complex(-x,-y);
    }
    
    /**
        String representation of this Complex number.
        @return x+i*y, x-i*y, x, or i*y as appropriate.
    */
    @Override
	public String toString() {
        if (x!=0 && y>0) {
            return x+" + "+y+"i";
        }
        if (x!=0 && y<0) {
            return x+" - "+(-y)+"i";
        }
        if (y==0) {
            return String.valueOf(x);
        }
        if (x==0) {
            return y+"i";
        }
        // shouldn't get here (unless Inf or NaN)
        return x+" + i*"+y;
        
    }  

    public double x() {
    	return x;
    }
    
    public double y() {
    	return y;
    }
    
    public Complex perpendicular(boolean clockwise) {
    	return clockwise? new Complex(-imag(), real()) : new Complex(imag(), -real());
    }
    
    public Complex perpendicular() {
    	return new Complex(imag(), -real());
    }
    
    public Complex normalize() {
    	double x = 0,y = 0;
        double length = Math.sqrt( this.real()*this.real() + this.imag()*this.imag() );
        if (length != 0) {
          x = this.real()/length;
          y = this.imag()/length;
        }

        return new Complex(x,y);
     }
    
    public Complex normalizeAndScale(double s) {
    	double x = 0,y = 0;
    	double oldLength = Math.sqrt( this.real()*this.real() + this.imag()*this.imag() );
    	if (oldLength != 0) {
    		x = this.real() * s / oldLength;
    		y = this.imag() * s / oldLength;
    	}
    	
    	return new Complex(x,y);
    }
    
    public int rotationalDirection(Complex c){
    	
    	//double x = (this.imag() * 0) - (0 * c.imag());
    	//double y = (0 * c.real()) - (this.real() * 0);
    	double z = (this.real() * c.imag()) - (this.imag() * c.real());
    	
    	return z < 0 ? -1 : 1;
    }
    
    public double scalar(Complex c){
    	return this.real() * c.real() + this.imag() * c.imag();
    }

	public double abs() {
		return Math.sqrt(real()*real()+imag()*imag());
	}
	
	public static float getRadius(Complex pointA, Complex pointB, Complex pointC){
		Complex p = getCentre(pointA, pointB);					
		Complex q = getCentre(pointB, pointC);
		
		Complex directedP = pointA.minus(p);
		Complex directedQ = pointB.minus(q);
		
		double scalarProd = pointB.minus(pointA).rotationalDirection(pointC.minus(pointB));
		
		Complex perpP = scalarProd > 0 ? new Complex(directedP.imag(), -directedP.real()) : new Complex(-directedP.imag(), directedP.real());
		Complex perpQ = scalarProd > 0 ? new Complex(directedQ.imag(), -directedQ.real()) : new Complex(-directedQ.imag(), directedQ.real());
		
		Complex k = p.plus(perpP);
		Complex m = q.plus(perpQ);
		
		Complex pointOfInterSection = getPointOfIntersection(p,k,q,m);
		
		return (float)pointOfInterSection.minus(pointA).abs();
	}
	
	public boolean nearBy(Complex p, double precision){
		return this.minus(p).abs() <= precision;
	}
	
	public static Complex getCentre(Complex p1, Complex p2){
		return p1.plus((p2.minus(p1).times(0.5)));		
	}
	
	public static Complex getPointOfIntersection(Complex p, Complex k, Complex q, Complex m){
		double s2 = ((((p.real()-q.real())/(m.real()-q.real())))-((p.imag()-q.imag())/(m.imag()-q.imag())))
				/(((k.imag()-p.imag())/(m.imag()-q.imag()))-((k.real()-p.real())/(m.real()-q.real())));
		
		double x = p.real() + s2 * (k.real() - p.real());
		double y = p.imag() + s2 * (k.imag() - p.imag());
		
		return new Complex(x,y);
	}
	
	public static Point getPoint(Complex complex){
		Point point = Point.T.create();
		point.setX(complex.x);
		point.setY(complex.y);
		return point;
	}
	
	public static Complex getComplex(Point point){
		return new Complex(point.getX(), point.getY());
	}
}
