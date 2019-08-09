
out vec4 outputColor;

uniform vec4 input_color;

uniform mat4 view_matrix;

// Light properties
uniform vec3 lightPos;
uniform vec3 lightIntensity;
uniform vec3 ambientIntensity;

// Material properties
uniform vec3 ambientCoeff;
uniform vec3 diffuseCoeff;
uniform vec3 specularCoeff;
uniform float phongExp;

uniform sampler2D tex;

// for fog
uniform vec3 skyColor;

in vec4 viewPosition;
in vec3 m;

in vec2 texCoordFrag;

void main()
{
    const float density = 0.08;
    const float gradient = 1.5;

    // Compute the s, v and r vectors
    //vec3 s = normalize(view_matrix*vec4(lightPos,1) - viewPosition).xyz;
    vec3 s = normalize(view_matrix*vec4(lightPos,0)).xyz;
    vec3 v = normalize(-viewPosition.xyz);
    vec3 r = normalize(reflect(-s,m));

    vec3 ambient = ambientIntensity*ambientCoeff;
    vec3 diffuse = max(lightIntensity*diffuseCoeff*dot(m,s), 0.0);
    vec3 specular;

    // Only show specular reflections for the front face
    if (dot(m,s) > 0)
        specular = max(lightIntensity*specularCoeff*pow(dot(r,v),phongExp), 0.0);
    else
        specular = vec3(0);

    vec4 ambientAndDiffuse = vec4(ambient + diffuse, 1);

    // distance
    float dist = length(viewPosition.xyz);
    float visibility = 1.0 / exp((dist * density) * (dist * density));
    visibility = clamp(visibility, 0.0, 1.0);

    outputColor = ambientAndDiffuse*input_color*texture(tex, texCoordFrag) + vec4(specular, 1);
    outputColor = mix(vec4(skyColor, 1.0), outputColor, visibility);
}
