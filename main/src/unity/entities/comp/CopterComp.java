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
abstract class CopterComp implements Unitc,Shieldc,Posc{
    transient RotorMount[] rotors;
    transient float rotorSpeedScl = 1f;

    @Import UnitType type;
    @Import boolean dead;
    @Import float x, y, rotation, elevation, maxHealth, drag, armor, hitSize, health, ammo, dragMultiplier,hitTime;
    @Import int id;
    @Import double flag=0;
    @Import UnitController controller;
    @Import Team team;
    @Import ItemStack stack;

    /** Absorbs health damage. */
    @Import float shield;
    /** Subtracts an amount from damage. No need to save. */

    @Import float shieldAlpha = 0f;
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

    }

    @Override
    public void setProp(UnlockableContent content, double value){
        if(content instanceof Item item){
            stack.item = item;
            stack.amount = Mathf.clamp((int)value, 0, type.itemCapacity);
        }
    }


}