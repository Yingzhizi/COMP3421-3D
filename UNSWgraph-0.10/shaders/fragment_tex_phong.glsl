out vec4 outputColor;

uniform vec4 input_color;

uniform mat4 view_matrix;

// Light properties
uniform vec3 lightPos;
uniform vec3 lightIntensity;
uniform vec3 sunlightIntensity;
uniform vec3 ambientIntensity;

// Material properties
uniform vec3 ambientCoeff;
uniform vec3 diffuseCoeff;
uniform vec3 specularCoeff;
uniform float phongExp;

uniform sampler2D tex;

in vec4 viewPosition;
in vec3 m;

in vec2 texCoordFrag;

void main()
{
    // Compute the s, v  vectors for sunlight
    vec3 s = normalize(view_matrix * vec4(lightPos,0)).xyz;
    vec3 v = normalize(-viewPosition.xyz);
    vec3 r = normalize(reflect(-s, m));

    vec3 ambient = ambientIntensity * ambientCoeff;
    vec3 diffuse = max(sunlightIntensity * diffuseCoeff * dot(normalize(m),s), 0.0);

    vec3 sunSpecular;
    if (dot(normalize(m), s) > 0) {
        sunSpecular = max(sunlightIntensity*specularCoeff*pow(dot(r,v),phongExp), 0.0);
    } else {
        sunSpecular = vec3(0);
    }

//    vec3 intensity = ambient + diffuse + specular;
//
//    outputColor = vec4(intensity,1)*input_color;


    vec4 ambientAndDiffuse = vec4(ambient + diffuse , 1);

    outputColor = (ambientAndDiffuse * input_color*texture(tex, texCoordFrag)) + vec4(sunSpecular, 1);
}