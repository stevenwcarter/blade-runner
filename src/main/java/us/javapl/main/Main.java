package us.javapl.main;

import de.vandermeer.asciitable.AsciiTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static final int DAGGER_OCV = 7;
    private static final int BLADE_OCV = 8;

    private static final Scanner keyboard = new Scanner(System.in);
    private static final Random random = new Random();

    public static void main(String[] args) {

        int enemyDc = getIntValueHidden("Enemy DC");
        int halfEnemyDc = (int)((double)enemyDc / 2d);
        int enemyPd = getIntValueHidden("Enemy PD");
        int enemyRpd = getIntValueHidden("enemy rPD");
        boolean coordinating = getIntValue("coordinating?") > 0;
        int bladeCount = getIntValue("Blade count");
        int daggerCount = getIntValue("Dagger count");

        int dcvReduction = 0;
        List<WeaponRoll> bladeRolls = new ArrayList<>();
        List<WeaponRoll> daggerRolls = new ArrayList<>();
        for (int i = 0; i<bladeCount; i++) {
            WeaponRoll bladeRoll = new WeaponRoll();
            if(coordinating) {
                bladeRoll.setCoordinated(roll3d6() <= 11);
                if (bladeRoll.isCoordinated()) {
                    dcvReduction++;
                }
            }
            bladeRoll.setAttack(roll3d6());
            if (bladeRoll.getAttack() == 3) {
                bladeRoll.setDamage(18);
            } else {
                bladeRoll.setDamage(roll3d6());
            }
            bladeRoll.setLocation(roll3d6());

            bladeRolls.add(bladeRoll);
        }
        for (int i = 0; i < daggerCount; i++) {
            WeaponRoll daggerRoll = new WeaponRoll();
            daggerRoll.setPenetrating(true);
            if(coordinating) {
                daggerRoll.setCoordinated(roll3d6() <= 11);
                if (daggerRoll.isCoordinated()) {
                    dcvReduction++;
                }
            }
            daggerRoll.setAttack(roll3d6());
            if (daggerRoll.getAttack() == 3) {
                daggerRoll.setDamage(6);
            } else {
                daggerRoll.setDamage(rolld6());
            }
            daggerRoll.setLocation(roll3d6());

            daggerRolls.add(daggerRoll);
        }

        int coordinatedDc = 18;
        if (coordinating) {
            coordinatedDc = enemyDc-dcvReduction;
            if (coordinatedDc<halfEnemyDc) {
                coordinatedDc = halfEnemyDc;
            }
        }

        AtomicInteger bodyTaken = new AtomicInteger(0);
        AtomicInteger stunTaken = new AtomicInteger(0);
        AtomicInteger coordinatedStun = new AtomicInteger(0);

        int finalEnemyDc = enemyDc;
        int finalCoordinatedDc = coordinatedDc;
        daggerRolls.forEach(daggerRoll -> {
            if (daggerRoll.getAttack() <= 11+(DAGGER_OCV- (coordinating && daggerRoll.isCoordinated() ? finalCoordinatedDc : finalEnemyDc))) {
                daggerRoll.setHit(true);
                if (coordinating && daggerRoll.isCoordinated()) {
                    int coordStun = Math.max(findStunMultiplier(daggerRoll.getLocation(), daggerRoll.getDamage()) - (enemyPd + enemyRpd),0);
                    daggerRoll.setActualStun(coordStun);
                    coordinatedStun.addAndGet(coordStun);
                } else {
                    int stun = Math.max(findStunMultiplier(daggerRoll.getLocation(), daggerRoll.getDamage()) - (enemyPd + enemyRpd),0);
                    daggerRoll.setActualStun(stun);
                    stunTaken.addAndGet(stun);
                }
                int daggerBody = Math.max(daggerRoll.getDamage() - enemyRpd, 0);
                if (daggerRoll.isPenetrating() && daggerBody < 1) {
                    daggerBody = 1;
                }

                int body = findBodyMultiplier(daggerRoll.getLocation(), daggerBody);
                daggerRoll.setActualBody(body);
                bodyTaken.addAndGet(body);
            }
        });
        bladeRolls.forEach(bladeRoll -> {
            if (bladeRoll.getAttack() <= 11+(BLADE_OCV- (coordinating && bladeRoll.isCoordinated() ? finalCoordinatedDc : finalEnemyDc))) {
                bladeRoll.setHit(true);
                if (coordinating && bladeRoll.isCoordinated()) {
                    int coordStun = Math.max(findStunMultiplier(bladeRoll.getLocation(), bladeRoll.getDamage()) - (enemyPd + enemyRpd),0);
                    bladeRoll.setActualStun(coordStun);
                    coordinatedStun.addAndGet(coordStun);
                } else {
                    int stun = Math.max(findStunMultiplier(bladeRoll.getLocation(), bladeRoll.getDamage()) - (enemyPd + enemyRpd), 0);
                    bladeRoll.setActualStun(stun);
                    stunTaken.addAndGet(stun);
                }
                int bladeBody = Math.max(bladeRoll.getDamage() - enemyRpd, 0);
                if (bladeRoll.isPenetrating() && bladeBody < 1) {
                    bladeBody = 1;
                }


                int body = findBodyMultiplier(bladeRoll.getLocation(), bladeBody);
                bladeRoll.setActualBody(body);
                bodyTaken.addAndGet(body);
            }
        });

        printChart(bladeRolls, daggerRolls, bodyTaken.get(), (stunTaken.get() + coordinatedStun.get()), coordinatedStun.get());
    }

    protected static int getIntValue(String prompt) {
        System.out.println(prompt+":");
        return keyboard.nextInt();
    }
    protected static int getIntValueHidden(String prompt) {
        EraserThread eraserThread = new EraserThread(prompt+":");
        Thread mask = new Thread(eraserThread);
        mask.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String password = "";

        try {
            password = in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // stop masking
        eraserThread.stopMasking();
        // return the password entered by the user
        return Integer.parseInt(password);
    }

    static int rolld6() {
        return random.nextInt(6)+1;
    }
    static int roll3d6() {
        return rolld6()+rolld6()+rolld6();
    }

    static int findBodyMultiplier(int location, int body) {
        switch(location) {
            case 3:
            case 4:
            case 13:
            case 5: return body * 2;
            case 6:
            case 7:
            case 15:
            case 16:
            case 17:
            case 18:
            case 8: return (body / 2);
            case 9:
            case 10:
            case 11:
            case 14:
            case 12: return body;
            default: {
                System.err.println("Missing handler for "+location);
                return body;
            }
        }
    }

    static int findStunMultiplier(int location, int body) {
        switch(location) {
            case 3:
            case 4:
            case 5: return body * 5;
            case 6:
            case 17:
            case 18: return body;
            case 7:
            case 8:
            case 14:
            case 15:
            case 16: return body * 2;
            case 9:
            case 10:
            case 11: return body * 3;
            case 13:
            case 12: return body * 4;
            default: {
                System.err.println("No handler for stun for " + location);
                return body;
            }
        }
    }

    static void printChart(List<WeaponRoll> bladeRolls, List<WeaponRoll> daggerRolls, int bodyTaken, int stunTaken, int coordinatedStun) {
        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("Type","Hit","Attack","Location","Damage","Coord","BODY","STUN");
        table.addRule();
        bladeRolls.forEach(roll -> {
            table.addRow("Blade", (roll.isHit()?"Yes":"No"), roll.getAttack(), roll.getLocation(), roll.getDamage(), (roll.isCoordinated()?"true":"false"), roll.getActualBody(), roll.getActualStun());
            table.addRule();
        });
        daggerRolls.forEach(roll -> {
            table.addRow("Dagger", (roll.isHit()?"Yes":"No"), roll.getAttack(), roll.getLocation(), roll.getDamage(), (roll.isCoordinated()?"true":"false"), roll.getActualBody(), roll.getActualStun());
            table.addRule();
        });
        System.out.println(table.render());

        AsciiTable damageTable = new AsciiTable();
        damageTable.addRule();
        damageTable.addRow("BODY", bodyTaken);
        damageTable.addRule();
        damageTable.addRow("STUN", stunTaken);
        damageTable.addRule();
        damageTable.addRow("COORD", coordinatedStun);
        damageTable.addRule();
        System.out.println("");
        System.out.println(damageTable.render(14));
    }
}
