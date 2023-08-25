package unity.entities.comp;

import arc.math.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.units.UnitController;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.logic.LAccess;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.UnitPayload;
import unity.annotations.Annotations.*;
import unity.entities.*;
import unity.entities.Rotor.*;
import unity.type.*;

import static mindustry.Vars.*;

/**
 * @author GlennFolker
 * @author MEEPofFaith
 */
@SuppressWarnings("unused")
@EntityComponent
abstract class CopterComp implements Unitc{
    transient RotorMount[] rotors;
    transient float rotorSpeedScl = 1f;

    @Import UnitType type;
    @Import boolean dead;
    @Import float x, y, rotation, elevation, maxHealth, drag, armor, hitSize, health, ammo, dragMultiplier;
    @Import int id;
    @Import double flag=0;
    @Import UnitController controller;
    @Import Team team;
    @Import ItemStack stack;
    @Override
    public void add(){
        UnityUnitType type = (UnityUnitType)this.type;

        rotors = new RotorMount[type.rotors.size];
        for(int i = 0; i < rotors.length; i++){
            Rotor rotor = type.rotors.get(i);
            rotors[i] = new RotorMount(rotor);
            rotors[i].rotorRot = rotor.rotOffset;
            rotors[i].rotorShadeRot = rotor.rotOffset;
        }
    }

    @Override
    public void update(){
        UnityUnitType type = (UnityUnitType)this.type;
        if(dead || health < 0f){
            if(!net.client() || isLocal()) rotation += type.fallRotateSpeed * Mathf.signs[id % 2] * Time.delta;

            rotorSpeedScl = Mathf.lerpDelta(rotorSpeedScl, 0f, type.rotorDeathSlowdown);
        }else{
            rotorSpeedScl = Mathf.lerpDelta(rotorSpeedScl, 1f, type.rotorDeathSlowdown);
        }

        for(RotorMount rotor : rotors){
            rotor.rotorRot += rotor.rotor.speed * rotorSpeedScl * Time.delta;
            rotor.rotorRot %= 360f;

            rotor.rotorShadeRot += rotor.rotor.shadeSpeed * Time.delta;
            rotor.rotorShadeRot %= 360f;
        }
    }

    @Override
    public void setProp(LAccess prop, double value){
        switch(prop){
            case health -> health = (float)Mathf.clamp(value, 0, maxHealth);
            case x -> x = World.unconv((float)value);
            case y -> y = World.unconv((float)value);
            case rotation -> rotation = (float)value;
            case team -> {
                if(!net.client()){
                    Team team = Team.get((int)value);
                    if(controller instanceof Player p){
                        p.team(team);
                    }
                    this.team = team;
                }
            }
            case flag -> flag = value;
        }
    }

    public void setProp(LAccess prop, Object value) {
        switch (prop) {
            case team:
                if (value instanceof Team) {
                    Team t = (Team)value;
                    if (!Vars.net.client()) {
                        UnitController var9 = this.controller;
                        if (var9 instanceof Player) {
                            Player p = (Player)var9;
                            p.team(t);
                        }

                        this.team = t;
                    }
                }
                break;
            case payloadType:
                if (this instanceof Payloadc) {
                    Payloadc pay = (Payloadc)this;
                    if (!Vars.net.client()) {
                        if (value instanceof Block bl) {
                            Block b = (Block)value;
                            Building build = b.newBuilding().create(b, this.team());
                            if (pay.canPickup(build)) {
                                pay.addPayload(new BuildPayload(build));
                            }
                        } else if (value instanceof UnitType) {
                            UnitType ut = (UnitType)value;
                            Unit unit = ut.create(this.team());
                            if (pay.canPickup(unit)) {
                                pay.addPayload(new UnitPayload(unit));
                            }
                        } else if (value == null && pay.payloads().size > 0) {
                            pay.dropLastPayload();
                        }
                    }
                }
        }

    }

    @Override
    public void setProp(UnlockableContent content, double value){
        if(content instanceof Item item){
            stack.item = item;
            stack.amount = Mathf.clamp((int)value, 0, type.itemCapacity);
        }
    }
}