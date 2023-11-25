package edu.upc.epsevg.prop.robocode;

import robocode.*;
import static robocode.util.Utils.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PredatorRobot extends TeamRobot {
  
  double dir = 1;
  boolean picaParet = false;
  
  String objectiu = "cap";
  double vidaobjeciu, distobjeciu;
  
  boolean pacific = false;
  
  boolean escollint = false;
  String enemics[];
  double disenemics[];
  
  @Override
  public void run() {
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForRobotTurn(true);
    setAdjustRadarForGunTurn(true);
    try {
        broadcastMessage("pos" + getX() + "," + getY());
      } catch (IOException ex) {
        Logger.getLogger(Droid.class.getName()).log(Level.SEVERE, null, ex);
      }
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
    if (getOthers() == 1 && !isTeammate(e.getName())) {
      objectiu = e.getName();
    } else {
      if (escollint) {
        for (int i = 0; i < enemics.length; i++) {
          if (enemics[i].equals(e.getName())) return;
          if (enemics[i].equals("")) {
            enemics[i] = e.getName();
            disenemics[i] = e.getDistance();
            if (i == enemics.length-1) {
              escollirObjectiuInd();
              String envia = "objectiu ,"+objectiu+","+distobjeciu;
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
        
      if(!pacific){
        double bearing = e.getBearing();
        double distance = e.getDistance();
        double height = getHeight();

        double angleToTurn;

        if (distance > height * 3) {
            angleToTurn = bearing + 90 - (40 * dir);
        } else {
            angleToTurn = bearing + 90 - (10 * dir);
        }

        setTurnRight(angleToTurn);

      }
      if(pacific){
        double bearing = e.getBearing();
        double distance = e.getDistance();
        double height = getHeight();

        double angleToTurn;

        if (distance > height * 3) {
            angleToTurn = bearing - (40 * dir);
        } else {
            angleToTurn = bearing - (10 * dir);
        }

        setTurnRight(angleToTurn);
      }
    }
    double absBearing = e.getBearing() + getHeading();
    setTurnRadarRight(ajustarRadar(absBearing));
    setTurnGunRightRadians(aimDef(e));
    if(e.getEnergy() > 45){
        setFire(piupiu(e.getDistance()));
        pacific = false;
    } else pacific = true;
    vidaobjeciu = e.getEnergy();
    distobjeciu = e.getDistance();
  }
  
  @Override
  public void onHitRobot(HitRobotEvent e) {
    if(!objectiu.equals(e.getName()) && !pacific){
        dir *= -1;
    }
  }
  
  @Override
  public void onHitWall(HitWallEvent e) {
    picaParet = true;
    if (dir == -1) {
        if (Math.abs(e.getBearing()) >= 160.0) {
            dir = 1;
        }
    } else if (dir == 1) {
        if (Math.abs(e.getBearing()) <= 20.0) {
            dir = -1;
        }
    }

    if (dir == 1) {
        setTurnRight(normalRelativeAngleDegrees(e.getBearing()));
        dir = -1;
    } else {
        setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 180));
        dir = 1;
    }

  }
  
  @Override
  public void onRobotDeath(RobotDeathEvent e) {
    if (e.getName().equals(objectiu)) {
        pacific = false;
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
  }
  
  @Override
  public void onMessageReceived(MessageEvent e){
      String walkieTalkie = e.getMessage().toString();
      if(walkieTalkie.contains("pos")) return;
      String missatge[] = walkieTalkie.split(",");
      String victima = missatge[1];
      double distancia = Double.parseDouble(missatge[2]);
      if(distobjeciu == 0.0 || distancia < distobjeciu) objectiu = victima;
      out.println("victima: " + victima);
      out.println("objectiu: " + victima);
      String envia = "objectiu ,"+objectiu+","+distobjeciu;
        out.println(envia);
        try {
          broadcastMessage(envia);
        } catch (IOException ex) {
          Logger.getLogger(Droid.class.getName()).log(Level.SEVERE, null, ex);
        }
  }
  
 double ajustarRadar(double absBearing) {
    if (getOthers() > 1) {
        double modR = 0.0;
        modR = normalRelativeAngleDegrees(absBearing - getRadarHeading()) + 22.5 * Math.signum(modR);
        modR = Math.max(-45.0, Math.min(45.0, modR));
        modR = Math.max(-20.0, Math.min(20.0, modR));
        return modR;
    } else {
        return normalRelativeAngleDegrees((absBearing - getRadarHeading()) * 2);
    }
}

  
    double aimDef(ScannedRobotEvent e) {
        if (e.getEnergy() != 0.0) {
            double absBearingRad = getHeadingRadians() + e.getBearingRadians();
            double prediction = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearingRad) / Rules.getBulletSpeed(piupiu(e.getDistance()));

            if (e.getDistance() <= 12 * 10) {
                prediction *= 0.5;
            } else if (e.getDistance() <= 12 * 5) {
                prediction *= 0.3;
            }

            return normalRelativeAngle(absBearingRad - getGunHeadingRadians() + prediction);
        } else {
            return normalRelativeAngle(getHeadingRadians() + e.getBearingRadians() - getGunHeadingRadians());
        }
    }

  
  double piupiu(double dobjeciu) {
    if (vidaobjeciu == 0.0) {
        return 0.1;
    }

    double distancia = getHeight() * 1.5;
    double energia = getEnergy();

    if (dobjeciu < distancia) {
        return Rules.MAX_BULLET_POWER;
    } else if ((energia > 4 * Rules.MAX_BULLET_POWER + 2 * (Rules.MAX_BULLET_POWER - 1) || (getOthers() == 1 && energia > 3 * Rules.MAX_BULLET_POWER)) && dobjeciu <= distancia * 2) {
        return Rules.MAX_BULLET_POWER;
    } else if (energia > 2.2) {
        return Math.min(1.1 + (distancia * 2) / dobjeciu, Rules.MAX_BULLET_POWER);
    } else {
        return Math.max(0.1, energia / 3);
    }
}

  
  boolean anemAxocar(double r) {
    return getX() + getHeight()*r >= getBattleFieldWidth() || getX() - getHeight()*r <= 0.0 || getY() + getHeight()*r >= getBattleFieldHeight() || getY() - getHeight()*r <= 0.0;
  }
}

