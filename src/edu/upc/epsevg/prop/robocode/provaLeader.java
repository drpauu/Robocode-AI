package edu.upc.epsevg.prop.robocode;

import robocode.*;
import static robocode.util.Utils.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class provaLeader extends TeamRobot {
  
  double d = 150.0, dir = 1;
  boolean picaParet = false;
  
  String objectiu = "cap";
  double vidaobjeciu, distobjeciu, Xobjectiu, Yobjectiu, ORIobj, SPobj;
  
  boolean pacific = false;
  double temps = 0.0; // pensar la variable temps
  
  boolean escollint = false;
  String enemics[];
  double disenemics[];
  double Xenemics[];
  double Yenemics[];
  double SP[];
  double ORI[];
  
  // provar que l array nomes sigui de 5, en comptes de que sigui getOthers();
  // que al final ens estalviem moltes linies
  
  @Override
  public void run() {
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForRobotTurn(true);
    setAdjustRadarForGunTurn(true);
    prepObjectiu();
    while (true) {
      setAhead((distobjeciu / 4 + 25) * dir);
      if (!picaParet && anemAxocar(3)) {
        dir *= -1;
        picaParet = true;
      } else if (!anemAxocar(3.2)) {
        picaParet = false;
      }
      execute();
    }
  }
  
  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    if (e.getName().equals(objectiu)) {
        String envia = (getX() + Math.sin(Math.toRadians(getHeading() + e.getBearing())) * e.getDistance()) + "," + (getY() + Math.cos(Math.toRadians(getHeading() + e.getBearing())) * e.getDistance()) + "," + e.getVelocity() + "," + e.getHeading();
        out.println("objectiu: " + objectiu + "  dades: " + envia);
        try {
          broadcastMessage(envia);
        } catch (IOException ex) {
          Logger.getLogger(Droid.class.getName()).log(Level.SEVERE, null, ex);
        }
        double absBearing = e.getBearing() + getHeading() - getRadarHeading();
        setTurnRadarRight(absBearing);
        return;
    }
    if (getOthers() == 1 && !isTeammate(e.getName())) {
      objectiu = e.getName();
    } else {
      if (escollint) {
        for (int i = 0; i < enemics.length; i++) {
          if (enemics[i].equals(e.getName())) return;
          if (enemics[i].equals("")) {
            enemics[i] = e.getName();
            disenemics[i] = e.getDistance();
            Xenemics[i] = getX() + Math.sin(getHeading() + e.getBearing()) * e.getDistance();
            Yenemics[i] = getY() + Math.cos(getHeading() + e.getBearing()) * e.getDistance();
            ORI[i] = e.getHeading();
            SP[i] = e.getVelocity();
            if (i == enemics.length-1) {
              escollirObjectiuInd();
              String envia = distobjeciu + "," + Xobjectiu + "," + Yobjectiu + "," + SPobj + "," + ORIobj;
              out.println(envia);
              try {
                broadcastMessage(envia);
              } catch (IOException ex) {
                Logger.getLogger(Droid.class.getName()).log(Level.SEVERE, null, ex);
              }
            }
            i = enemics.length;
          }
        }
        return;
      }
    }
    if (!e.getName().equals(objectiu)) return;
    if (!anemAxocar(0.6)) {
        
      if(!pacific)setTurnRight(e.getBearing() + 90 -(e.getDistance() > getHeight()*3 ? 40 : 10) * dir);
      if(pacific)setTurnRight(e.getBearing()-(e.getDistance() > getHeight()*3 ? 40 : 10) * dir);
    }
    double absBearing = e.getBearing() + getHeading();
    setTurnRadarRight(ajustarRadar(absBearing));
    pacific = true;
    vidaobjeciu = e.getEnergy();
    distobjeciu = e.getDistance();
  }
  
  @Override
  public void onHitRobot(HitRobotEvent e) {
    if(!objectiu.equals(e.getName())){
        dir *= -1;
    }
  }
  
  @Override
  public void onHitWall(HitWallEvent e) {
    picaParet = true;
    if (dir == -1 && Math.abs(e.getBearing()) >= 160.0) {
      dir = 1;
    } else if (dir == 1 && Math.abs(e.getBearing()) <= 20.0) {
      dir = -1;
    } else {
      if (dir == 1) {
        setTurnRight(normalRelativeAngleDegrees(e.getBearing()));
        dir = -1;
      } else {
        setTurnRight(normalRelativeAngleDegrees(e.getBearing()+180));
        dir = 1;
      }
    }
  }
  
  @Override
  public void onRobotDeath(RobotDeathEvent e) {
      pacific = false;
    if (e.getName().equals(objectiu)) {
      if (getOthers() > 1) {
        prepObjectiu();
      } else {
        setTurnRadarRight(Double.POSITIVE_INFINITY);
      }
    } else if (escollint) {
      prepObjectiu();
    }
  }
  
  void prepObjectiu() {
      pacific = false;
    enemics = new String[getOthers()];
    disenemics = new double[getOthers()];
    Xenemics = new double[getOthers()];
    Yenemics = new double[getOthers()];
    ORI = new double[getOthers()];
    SP = new double[getOthers()];
    for (int i = 0; i < enemics.length; i++) {
      enemics[i] = "";
    }
    escollint = true;
    setTurnRadarRight(Double.POSITIVE_INFINITY);
  }

  void escollirObjectiuInd() {
    escollint = false;
    int victima = -1;
    double menorDis = Double.MAX_VALUE;
    for (int i = 0; i < enemics.length; i++) {
      if (disenemics[i] < menorDis && !isTeammate(enemics[i])) {
        victima = i;
        menorDis = disenemics[i];
      }
    }
    objectiu = enemics[victima];
    distobjeciu = disenemics[victima];
    Xobjectiu = Xenemics[victima];
    Yobjectiu = Yenemics[victima];
    ORIobj = ORI[victima];
    SPobj = SP[victima];
  }
  
  
  // en aquesta mateix fucnio es pot canviar l enemic, de manera que cada cop que rep
  // un missatge canvia o no l objectiu, i aixi esta sempre en constant actualitxacio
  
  double ajustarRadar(double absBearing) {
    if (getOthers() > 1) {
      double modR;
      modR = normalRelativeAngleDegrees((absBearing - getRadarHeading()));
      modR += 22.5*Math.signum(modR);
      if (modR > 45.0) {
        modR = 45.0;
      } else if (modR < -45.0) {
        modR = -45.0;
      } else if (modR > 0.0 && modR < 20.0) {
        modR = 20.0;
      } else if (modR > -20.0 && modR <= 0.0) {
        modR = -20.0;
      }
      return modR;
    } else {
      return normalRelativeAngleDegrees((absBearing - getRadarHeading())*2);
    }
  }
  
  boolean anemAxocar(double r) {
    return getX() + getHeight()*r >= getBattleFieldWidth() || getX() - getHeight()*r <= 0.0 || getY() + getHeight()*r >= getBattleFieldHeight() || getY() - getHeight()*r <= 0.0;
  }
}

