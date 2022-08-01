package us.javapl.main;

import java.util.Objects;

public class WeaponRoll {

    private int ocv;
    private boolean penetrating = false;
    private int attack;
    private int location;

    private int damage;
    private boolean coordinated = false;
    private int actualBody;
    private int actualStun;
    private boolean hit = false;


    public int getOcv() {
        return ocv;
    }

    public void setOcv(int ocv) {
        this.ocv = ocv;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public boolean isPenetrating() {
        return penetrating;
    }

    public void setPenetrating(boolean penetrating) {
        this.penetrating = penetrating;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public boolean isCoordinated() {
        return coordinated;
    }

    public void setCoordinated(boolean coordinated) {
        this.coordinated = coordinated;
    }

    public int getActualBody() {
        return actualBody;
    }

    public void setActualBody(int actualBody) {
        this.actualBody = actualBody;
    }

    public int getActualStun() {
        return actualStun;
    }

    public void setActualStun(int actualStun) {
        this.actualStun = actualStun;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeaponRoll that = (WeaponRoll) o;
        return getOcv() == that.getOcv() && isPenetrating() == that.isPenetrating() && getAttack() == that.getAttack() && getLocation() == that.getLocation() && getDamage() == that.getDamage() && isCoordinated() == that.isCoordinated();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOcv(), isPenetrating(), getAttack(), getLocation(), getDamage(), isCoordinated());
    }
}
