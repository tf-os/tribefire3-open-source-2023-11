package tribefire

import (
	"github.com/stretchr/testify/assert"
	"testing"
	tribefirev1 "tribefire-operator/api/v1"
)

func TestSplitRuntimeName(t *testing.T) {
	var tests = []struct {
		runtimeName string
		stageLabel  string
		want        []string
	}{
		{"tfdemo-stable", "default", []string{"tfdemo-stable", ""}},
		{"tfdemo-business-stable", "stable", []string{"tfdemo-business", "stable"}},
		{"tfdemo-dev", "dev", []string{"tfdemo", "dev"}},
		{"tfdemo-dev", "default", []string{"tfdemo-dev", ""}},
		{"dev-tfdemo", "dev", []string{"tfdemo", "dev"}},
		{"stable-tfdemo-dev", "dev", []string{"stable-tfdemo", "dev"}},
		{"dev-and-stable-tfdemo", "dev", []string{"and-stable-tfdemo", "dev"}},
		{"stable-tfdemo-more-than-it-makes-no-sense-dev", "dev", []string{"stable-tfdemo-more-than-it-makes-no-sense", "dev"}},
		{"stable-dev", "dev", []string{"stable", "dev"}},
		{"test-initial", "default", []string{"test-initial", ""}},
		{"test-initial-default", "default", []string{"test-initial-default", ""}},
	}

	for _, tt := range tests {
		t.Run(tt.runtimeName, func(t *testing.T) {
			tf := &tribefirev1.TribefireRuntime{}
			tf.Name = tt.runtimeName
			labels := make(map[string]string)
			labels["stage"] = tt.stageLabel
			tf.SetLabels(labels)
			name, stage := SplitRuntimeName(tf)
			assert.Equal(t, tt.want[0], name, "name part must match")
			assert.Equal(t, tt.want[1], stage, "stage part must match")
		})
	}

}
