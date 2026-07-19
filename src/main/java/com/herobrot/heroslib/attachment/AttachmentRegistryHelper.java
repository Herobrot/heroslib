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
import java.util.function.UnaryOperator;

public class AttachmentRegistryHelper {

    @SuppressWarnings("unused")
    public static DeferredRegister<AttachmentType<?>> createRegister(String modId) {
        return DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, modId);
    }

    /**
     * Registra un Attachment basado en NBT con validación estricta de tipos.
     */
    @SuppressWarnings("unused")
    public static <H extends IAttachmentHolder, T extends INBTSyncable> Supplier<AttachmentType<T>> registerNBTAttachment(
            DeferredRegister<AttachmentType<?>> register,
            String name,
            Class<H> holderType,
            Function<H, T> factory,
            boolean copyOnDeath
    ) {
        // Llama a la función principal con un modificador vacío (identity)
        return registerNBTAttachment(register, name, holderType, factory, copyOnDeath, UnaryOperator.identity());
    }

    /**
     * Registra un Attachment basado en NBT permitiendo modificar el Builder interno de NeoForge (Ej. para añadir .sync()).
     */
    @SuppressWarnings("unused")
    public static <H extends IAttachmentHolder, T extends INBTSyncable> Supplier<AttachmentType<T>> registerNBTAttachment(
            DeferredRegister<AttachmentType<?>> register,
            String name,
            Class<H> holderType,
            Function<H, T> factory,
            boolean copyOnDeath,
            UnaryOperator<AttachmentType.Builder<T>> customizer
    ) {
        // Fábrica segura: Valida el tipo antes de hacer el cast
        Function<IAttachmentHolder, T> safeFactory = holder -> {
            if (!holderType.isInstance(holder)) {
                throw new IllegalStateException(
                        "HerosLib: El Attachment '" + name + "' esperaba un holder de tipo " + holderType.getSimpleName()
                                + " pero recibio " + holder.getClass().getSimpleName()
                );
            }
            return factory.apply(holderType.cast(holder));
        };

        return register.register(name, () -> {
            var builder = AttachmentType.builder(safeFactory)
                    .serialize(new IAttachmentSerializer<CompoundTag, T>() {
                        @Override
                        public @NotNull T read(@NotNull IAttachmentHolder holder, @NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
                            T instance = safeFactory.apply(holder);
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

            if (copyOnDeath) {
                builder.copyOnDeath();
            }

            // Aplicamos cualquier personalización extra que el mod dependiente haya solicitado
            return customizer.apply(builder).build();
        });
    }

    public interface INBTSyncable {
        void readNbt(CompoundTag tag);
        void writeNbt(CompoundTag tag);
    }
}