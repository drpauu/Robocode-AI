package edu.upc.epsevg.prop.robocode;

import robocode.*;

public class provaDroid extends TeamRobot implements Droid
{
    double bear;
    boolean move = true;
    @Override
    public void run() {
        setAdjustGunForRobotTurn(true);
        while (true) {
            if (move == true) {
                setAhead(10);
                setTurnRight(bear);
            }
            execute();
        }
    }
    
    double _X;
    double _Y;
    double X;
    double Y;
    double SP;
    double ORI;
    double _B;
    
    @Override
    public void onMessageReceived(MessageEvent e)
    {
        String walkieTalkie = e.getMessage().toString();
        String missatge[] = walkieTalkie.split(",");
        _X = Double.parseDouble(missatge[0]);
        _Y = Double.parseDouble(missatge[1]);
        SP = Double.parseDouble(missatge[2]);
        ORI = Double.parseDouble(missatge[3]);
        bear = Math.toDegrees(Math.atan((_X-getX()) / (_Y-getY())));
        bear = (bear - getHeading()) % 360;
        if (bear < -180) bear = 360 + bear;
        if (bear > 180) bear = -360 + bear;
        
        if (move == true) {
                setAhead(10);
                setTurnRight(bear);
        }
        
        double _finalRotate = aimCalc();
        _finalRotate = _finalRotate%360;
        if (_finalRotate < -180) _finalRotate = 360.0 - _finalRotate;
        if (_finalRotate > 180) _finalRotate = -360.0 + _finalRotate;
        if (_finalRotate > 0) setTurnGunRight(_finalRotate);
        else setTurnGunLeft(-_finalRotate);
        fire(Rules.MAX_BULLET_POWER);
        move = true;
    }
    
    public double aimCalc() {
        
        double _A = Math.cos(Math.toRadians(ORI)) / Math.sin(Math.toRadians(ORI));
        _B =  _Y-_X*_A;
        X = _X;
        Y = _X*_A + _B;
        double d = aprox(X, Y, SP), minX = X, minD = d, maxX = Double.MAX_VALUE, maxD = Double.MAX_VALUE;
        boolean goUp = false;
        for (int i = 0; i < 40; i++) {
            d = aprox (X, Y, SP);
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
                X = X + 200 * Math.sin(Math.toRadians(ORI));
                Y = Y + 200 * Math.cos(Math.toRadians(ORI));
            }
        }
        if (Math.abs(minD) < Math.abs(maxD)) X = minX;
        else X = maxX;
        Y = _A*X + _B;
        double _angle = Math.toDegrees(Math.atan((X-getX()) / (Y-getY())));
        if ((Y-getY()) < 0) _angle += 180;
        double ret = _angle - getGunHeading();
        return ret;
    }
    
   public double PowTwo(double a){
        return a*a;
    }
    
    public double aprox(double X, double Y, double speed) {
        double a = Math.sqrt( PowTwo(X - _X) + PowTwo(Y - _Y)) - (speed * Math.sqrt( PowTwo(X - getX()) + PowTwo(Y - getY())) / Rules.getBulletSpeed(Rules.MAX_BULLET_POWER));    
        return a;
    }
    
}
