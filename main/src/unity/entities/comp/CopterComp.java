package unity.entities.comp;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.ai.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.payloads.*;
import unity.annotations.Annotations.*;
import unity.entities.*;
import unity.entities.Rotor.*;
import unity.type.*;

import static mindustry.Vars.*;
import static mindustry.Vars.*;
import static mindustry.logic.GlobalVars.*;

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
    @Import float x, y, rotation, elevation, maxHealth, drag, armor, hitSize, health, shield, ammo, dragMultiplier;
    @Import Team team;
    @Import int id;

    @Import UnitController controller;
    double flag;
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
    // TODO : Missing setProp in comp.
    public void setProp(LAccess prop, double value){
        switch(prop){
            case health -> {
                health = (float)Mathf.clamp(value, 0, maxHealth);
                if(health <= 0f && !dead){
                    kill();
                }
            }
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
    public void setProp(LAccess prop, Object value){
        switch(prop){
            case team -> {
                if(value instanceof Team t && !net.client()){
                    if(controller instanceof Player p) p.team(t);
                    team = t;
                }
            }
            case payloadType -> {
                //only serverside
                if(((Object)this) instanceof Payloadc pay && !net.client()){
                    if(value instanceof Block b){
                        Building build = b.newBuilding().create(b, team());
                        if(pay.canPickup(build)) pay.addPayload(new BuildPayload(build));
                    }else if(value instanceof UnitType ut){
                        Unit unit = ut.create(team());
                        if(pay.canPickup(unit)) pay.addPayload(new UnitPayload(unit));
                    }else if(value == null && pay.payloads().size > 0){
                        pay.payloads().pop();
                    }
                }
            }
        }
    }

    public void setProp(UnlockableContent content, double value){
        if(content instanceof Item item){
            stack.item = item;
            stack.amount = Mathf.clamp((int)value, 0, type.itemCapacity);
        }
    }

}
