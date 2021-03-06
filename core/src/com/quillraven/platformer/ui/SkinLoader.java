package com.quillraven.platformer.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/*
 * Created by Quillraven on 12.02.2018.
 *
 * MIT License
 *
 * Copyright (c) 2018 Quillraven
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

/**
 * Custom SkinLoader class for our custom {@link Skin} class to create bitmap fonts on runtime
 * according to the density of the display.
 * <p></p>
 * Code is very similar to the original SkinLoader code except
 * that {@link #getDependencies(String, FileHandle, SkinParameter)} also includes bitmap font
 * dependencies according to the given {@link SkinParameter} to load the bitmap fonts before
 * loading the skin.
 * Also {@link #loadSync(AssetManager, String, FileHandle, SkinParameter)} is creating our own
 * {@link Skin} instance.
 */
public class SkinLoader extends AsynchronousAssetLoader<Skin, SkinLoader.SkinParameter> {
    public SkinLoader(final FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Array<AssetDescriptor> getDependencies(final String fileName, final FileHandle file, final SkinParameter parameter) {
        if (parameter == null) {
            throw new GdxRuntimeException("SkinParameter cannot be null");
        }

        // texture atlas dependency
        final Array<AssetDescriptor> dependencies = new Array<>();
        dependencies.add(new AssetDescriptor(file.pathWithoutExtension() + ".atlas", TextureAtlas.class));

        // bitmap font dependencies
        for (int fontSize : parameter.fontSizesToCreate) {
            final FreetypeFontLoader.FreeTypeFontLoaderParameter fontParam = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
            fontParam.fontFileName = parameter.fontPath;
            // enable anti-aliasing
            fontParam.fontParameters.minFilter = Texture.TextureFilter.Linear;
            fontParam.fontParameters.magFilter = Texture.TextureFilter.Linear;
            // create font according to density of target device display
            fontParam.fontParameters.size = (int) (fontSize * Gdx.graphics.getDensity());
            dependencies.add(new AssetDescriptor("font" + fontSize + ".ttf", BitmapFont.class, fontParam));
        }

        return dependencies;
    }

    @Override
    public Skin loadSync(final AssetManager manager, final String fileName, final FileHandle file, final SkinParameter parameter) {
        // load atlas and create skin
        final String textureAtlasPath = file.pathWithoutExtension() + ".atlas";
        final TextureAtlas atlas = manager.get(textureAtlasPath, TextureAtlas.class);
        final Skin skin = new Skin(atlas);

        // add bitmap fonts to skin
        for (int fontSize : parameter.fontSizesToCreate) {
            skin.add("font_" + fontSize, manager.get("font" + fontSize + ".ttf"));
        }

        // load skin now because the fonts in the json file are now available
        skin.load(file);
        return skin;
    }

    @Override
    public void loadAsync(final AssetManager manager, final String fileName, final FileHandle file, final SkinParameter parameter) {

    }

    public static class SkinParameter extends AssetLoaderParameters<Skin> {
        private final String fontPath;
        private final int[] fontSizesToCreate;

        public SkinParameter(final String fontPath, final int... fontSizesToCreate) {
            if (fontPath == null || fontPath.trim().isEmpty()) {
                throw new GdxRuntimeException("fontPath cannot be null or empty");
            }
            if (fontSizesToCreate.length == 0) {
                throw new GdxRuntimeException("fontSizesToCreate has to contain at least one value");
            }

            this.fontPath = fontPath;
            this.fontSizesToCreate = fontSizesToCreate;
        }
    }
}
