package unity.entities.comp;

import arc.math.*;
import arc.util.*;
import unity.annotations.Annotations.*;
import mindustry.gen.*;

//basically just TimedComp but kills instead of removing.
@SuppressWarnings("unused")
@EntityComponent
abstract class TestComp implements Entityc, Healthc, Scaled,Unitc{
    float time, lifetime;

    //called last so pooling and removal happens then.
    @MethodPriority(100)
    @Override
    public void update(){
        time = Math.min(time + Time.delta, lifetime);

        if(time >= lifetime){
            kill();
        }
    }

    @Override
    public float fin(){
        return time / lifetime;
    }
}
