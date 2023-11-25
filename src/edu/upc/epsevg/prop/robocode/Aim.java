package edu.upc.epsevg.prop.robocode;

import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot; 
import java.awt.Color;
import java.awt.Graphics2D;

public class Aim extends TeamRobot{
    
    public class Pair {
        
    }
    
    boolean rot1 = false, rot2 = false;
    @Override
    public void run() {
        //fem independents els diferents elements del tanc
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        while(true) {
            if (rot1==false && rot2 == false)setTurnRadarRight(5000);
            if (rot1 == false) rot2 = false;
            rot1 = false;
            execute();
        }
    }
    
    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        
        double Rotate = event.getBearing();
        double gunDir = getRadarHeading();
        double Dir = getHeading();
        
        double finalRotate = Dir - gunDir + Rotate;
        finalRotate = finalRotate % 360;
        if (finalRotate < -180) finalRotate = 360.0 - finalRotate;
        if (finalRotate > 180) finalRotate = -360.0 + finalRotate;
        if(finalRotate >= 0)setTurnRadarRight(finalRotate);
        else setTurnRadarLeft(-finalRotate);
        
        rot1 = true; rot2 = true;
        
        double _finalRotate = aimCalc(event);
        _finalRotate = _finalRotate%360;
        if (_finalRotate < -180) _finalRotate = 360.0 - _finalRotate;
        if (_finalRotate > 180) _finalRotate = -360.0 + _finalRotate;
        if (_finalRotate > 0) setTurnGunRight(_finalRotate);
        else setTurnGunLeft(-_finalRotate);
        //System.out.println("final rotate: " + _finalRotate);
        fire(Rules.MAX_BULLET_POWER);
    }
    
    double _X;
    double _Y;
    double _B;
    double X;
    double Y;
    public double aimCalc(ScannedRobotEvent event) {

        //return finalRotate;
        _X = Math.sin(Math.toRadians(event.getBearing() + getHeading())) * event.getDistance() + getX();
        _Y = Math.cos(Math.toRadians(event.getBearing() + getHeading())) * event.getDistance() + getY();
        double _A = Math.cos(Math.toRadians(event.getHeading())) / Math.sin(Math.toRadians(event.getHeading()));
        _B =  _Y-_X*_A;
        X = _X;
        Y = _X*_A + _B;
        double d = aprox(X, Y, event.getVelocity()), minX = X, minD = d, maxX = Double.MAX_VALUE, maxD = Double.MAX_VALUE;
        boolean goUp = false;
        /*X = X + 100 * Math.sin(Math.toRadians(event.getHeading()));
        Y = X*_A + _B;*/
        for (int i = 0; i < 40; i++) {
            //System.out.println("it: " +i);
            d = aprox (X, Y, event.getVelocity());
            /*System.out.println("d: " + d);
            System.out.println("X: " + X);
            System.out.println("minX: " + minX);*/
            //System.out.println("maxX: " + maxX);
            if (d > minD && d < 0) {
                minD = d;
                minX = X;
            }
            else if (d < maxD && d > 0) {
                maxD = d;
                maxX = X;
                goUp = true;
            }
            if (goUp == true){
                X = (minX + maxX) / 2;
                Y = X*_A + _B;
            }
            else {
                X = X + 200 * Math.sin(Math.toRadians(event.getHeading()));
                Y = Y + 200 * Math.cos(Math.toRadians(event.getHeading()));
            }
        }
        if (Math.abs(minD) < Math.abs(maxD)) X = minX;
        else X = maxX;
        Y = _A*X + _B;
        double _angle = Math.toDegrees(Math.atan((X-getX()) / (Y-getY())));
        if ((Y-getY()) < 0) _angle += 180;
        System.out.println("_angle: " + _angle);
        System.out.println("getGunHeading(): " + getGunHeading());
        double ret = _angle - getGunHeading();
        System.out.println("diff: " + ret);
        /*System.out.println("ofi: " + (event.getBearing() + getHeading()));
        System.out.println("angle: " + _angle + " heading: " + getHeading());*/
        
        //System.out.println("X: " +  (X-getX()) + " Y: " + (Y-getY()));
        return ret;
        //return event.getBearing() - getGunHeading() + getHeading();
    }
    
// Paint a transparent square on top of the last scanned robot
public void onPaint(Graphics2D g) {
    // Set the paint color to a red half transparent color
    g.setColor(new Color(0xff, 0x00, 0x00, 0x80));
    int _drawX = (int) _X;
    int _drawY = (int) _Y;
    int drawX = (int) X;
    int drawY = (int) Y;
    // Draw a line from our robot to the scanned robot
    g.drawLine( _drawX,  _drawY, drawX, drawY);
    g.drawLine( -1000,(int)_B, 1000, (int)_B);
    g.drawLine( -1000,0, 1000, 0);
    g.drawLine( 0,-1000, 0, 1000);
    // Draw a filled square on top of the scanned robot that covers it
    g.fillRect( drawX - 20,  drawY - 20, 40, 40);
}
    
   public double PowTwo(double a){
        return a*a;
    }
    
    public double aprox(double X, double Y, double speed) {
        //System.out.println("stoopid: " + X + " : " + Y + " : " + speed + " : " + Rules.getBulletSpeed(Rules.MAX_BULLET_POWER));
        double a = Math.sqrt( PowTwo(X - _X) + PowTwo(Y - _Y)) - (speed * Math.sqrt( PowTwo(X - getX()) + PowTwo(Y - getY())) / Rules.getBulletSpeed(Rules.MAX_BULLET_POWER));    
        //System.out.println("stoopid: " + a);
        return a;
    }
}

