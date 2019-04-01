/*
 * Copyright (C) 2018 InsomniaKitten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gegy1000.psf.server.block;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface Machine {
    // TODO Setup machines to toggle this for actual active state in TE's
    // Do we need to serialize this? It could probably be inferred from the TE
    PropertyBool ACTIVE = PropertyBool.create("active");

    default boolean isIdle(IBlockState state) {
        return !isActive(state);
    }

    default boolean isActive(IBlockState state) {
        return state.getValue(ACTIVE);
    }
}
