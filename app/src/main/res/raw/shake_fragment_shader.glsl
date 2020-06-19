#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTexCoord;
uniform samplerExternalOES uTexture;
//颜色的偏移距离
uniform float uTextureCoordOffset;
void main() {
    vec4 blue = texture2D(uTexture,vTexCoord);
    vec4 green = texture2D(uTexture,vec2(vTexCoord.x + uTextureCoordOffset,vTexCoord.y + uTextureCoordOffset));
    vec4 red = texture2D(uTexture,vec2(vTexCoord.x - uTextureCoordOffset,vTexCoord.y - uTextureCoordOffset));
    gl_FragColor = vec4(red.x,green.y,blue.z,blue.w);
}