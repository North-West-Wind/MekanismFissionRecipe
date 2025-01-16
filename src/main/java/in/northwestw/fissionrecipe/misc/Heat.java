package in.northwestw.fissionrecipe.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Heat {
    public static final MapCodec<Heat> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("isEqt").forGetter(Heat::isEqt),
            Codec.DOUBLE.fieldOf("constant").forGetter(Heat::getConstant),
            Codec.STRING.fieldOf("equation").forGetter(Heat::getEquation)
    ).apply(instance, Heat::new));
    public static final StreamCodec<ByteBuf, Heat> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, Heat::isEqt,
            ByteBufCodecs.DOUBLE, Heat::getConstant,
            ByteBufCodecs.STRING_UTF8, Heat::getEquation,
            Heat::new
    );
    public static final ScriptEngine JS_ENGINE;

    static {
        ScriptEngineManager mgr = new ScriptEngineManager();
        JS_ENGINE = mgr.getEngineByName("JavaScript");
    }

    private final boolean isEqt;
    private final double constant;
    private final String equation;

    public Heat(boolean isEqt, double constant, String equation) {
        this.isEqt = isEqt;
        if (this.isEqt) {
            this.constant = 0.2;
            this.equation = equation;
        } else {
            this.constant = constant;
            this.equation = null;
        }
    }

    public boolean isEqt() {
        return isEqt;
    }

    public double getConstant() {
        return constant;
    }

    public String getEquation() {
        return equation;
    }
}
