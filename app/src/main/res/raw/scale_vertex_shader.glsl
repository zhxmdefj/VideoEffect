//attribute vec2 aPosition;//顶点位置
attribute vec4 aPosition;//顶点位置
attribute vec4 aTexCoord;//纹理坐标
varying vec2 vTexCoord;
uniform mat4 uMvpMatrix;//缩放矩阵
uniform mat4 uTexMatrix;
void main() {
//    gl_Position = uMvpMatrix * vec4(aPosition,0.1,1.0);
    gl_Position = uMvpMatrix * aPosition;
    vTexCoord = (uTexMatrix * aTexCoord).xy;
}