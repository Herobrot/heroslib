package com.herobrot.heroslib.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class AttachmentRegistryHelper {

    public static DeferredRegister<AttachmentType<?>> createRegister(String modId) {
        return DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, modId);
    }

    public static <T extends INBTSyncable> Supplier<AttachmentType<T>> registerNBTAttachment(
            DeferredRegister<AttachmentType<?>> register,
            String name,
            Function<IAttachmentHolder, T> factory,
            boolean copyOnDeath
    ) {
        return register.register(name, () -> {
            var builder = AttachmentType.builder(factory)
                    .serialize(new IAttachmentSerializer<CompoundTag, T>() {
                        @Override
                        public @NotNull T read(@NotNull IAttachmentHolder holder, @NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
                            T instance = factory.apply(holder);
                            instance.readNbt(tag);
                            return instance;
                        }

                        @Override
                        public @NotNull CompoundTag write(@NotNull T attachment, HolderLookup.@NotNull Provider provider) {
                            CompoundTag tag = new CompoundTag();
                            attachment.writeNbt(tag);
                            return tag;
                        }
                    });

            if (copyOnDeath) builder.copyOnDeath();

            return builder.build();
        });
    }

    public interface INBTSyncable {
        void readNbt(CompoundTag tag);
        void writeNbt(CompoundTag tag);
    }
}