package tribefire

import (
	"github.com/stretchr/testify/assert"
	"os"
	"testing"
)

func TestCreateServiceAccount(t *testing.T) {
	tf, _ := createDemoRuntime()

	_ = os.Setenv("TRIBEFIRE_OPERATOR_DUMP_RESOURCES_STDOUT", "true")

	serviceAccount := NewServiceAccount(tf)
	role := NewRole(tf)
	roleBinding := NewRoleBinding(tf)

	assert.Equal(t, tf.Name, serviceAccount.Name)
	assert.Equal(t, tf.Name, role.Name)
	assert.Equal(t, tf.Name, roleBinding.Name)
}
