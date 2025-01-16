package in.northwestw.fissionrecipe.misc;

import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CodecException;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public record Heat(boolean isEqt, double constant, String equation) {
    public static final StreamCodec<ByteBuf, Heat> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, Heat::isEqt,
            ByteBufCodecs.DOUBLE, Heat::constant,
            ByteBufCodecs.STRING_UTF8, Heat::equation,
            Heat::new
    );
    public static final ScriptEngine JS_ENGINE;

    static {
        ScriptEngineManager mgr = new ScriptEngineManager();
        JS_ENGINE = mgr.getEngineByName("JavaScript");
    }

    public Heat(boolean isEqt, double constant, String equation) {
        this.isEqt = isEqt;
        if (this.isEqt) {
            this.constant = 0.2;
            this.equation = equation;
        } else {
            this.constant = constant;
            this.equation = "";
        }
    }

    public Either<Double, String> getEither() {
        return this.isEqt ? Either.right(this.equation) : Either.left(this.constant);
    }

    public static Heat fromEither(Either<Double, String> either) {
        if (either.left().isPresent()) return new Heat(false, either.left().get(), "");
        if (either.right().isPresent()) return new Heat(true, 0, either.right().get());
        throw new CodecException("Both left and right of either is empty");
    }
}
