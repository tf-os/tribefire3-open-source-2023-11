package controllers

import (
	"github.com/stretchr/testify/assert"
	meta "k8s.io/apimachinery/pkg/apis/meta/v1"
	"testing"
	"time"
	tribefirev1 "tribefire-operator/api/v1"

	core "k8s.io/api/core/v1"
)

func TestHandleNoExistingConditions(t *testing.T) {
	tf := &tribefirev1.TribefireRuntime{}
	tf = updateConditions(tf, false)
	assert.Equal(t, tribefirev1.TribefireRuntimeAvailable, tf.Status.Conditions[0].Type)
	assert.Equal(t, core.ConditionTrue, tf.Status.Conditions[0].Status)
}

func TestHandleExistingProgressingNoAvailableCondition(t *testing.T) {
	tf := &tribefirev1.TribefireRuntime{}
	runtimeProgressing := tribefirev1.TribefireRuntimeCondition{
		Type:               tribefirev1.TribefireRuntimeProgressing,
		Status:             core.ConditionTrue,
		LastTransitionTime: meta.NewTime(time.Now()),
		LastUpdateTime:     meta.NewTime(time.Now()),
		Message:            "TribefireRuntime is being created",
		Reason:             "TribefireRuntimeCreated",
	}

	tf.Status.Conditions = append(tf.Status.Conditions, runtimeProgressing)

	tf = updateConditions(tf, false)
	assert.Equal(t, tribefirev1.TribefireRuntimeProgressing, tf.Status.Conditions[0].Type)
	assert.Equal(t, core.ConditionTrue, tf.Status.Conditions[0].Status)
	assert.Equal(t, tribefirev1.TribefireRuntimeAvailable, tf.Status.Conditions[1].Type)
	assert.Equal(t, core.ConditionTrue, tf.Status.Conditions[1].Status)
	assert.Equal(t, "TribefireRuntimeBecameAvailable", tf.Status.Conditions[1].Reason)
}

func TestHandleExistingProgressingNoAvailableConditionDegraded(t *testing.T) {
	tf := &tribefirev1.TribefireRuntime{}
	runtimeProgressing := tribefirev1.TribefireRuntimeCondition{
		Type:               tribefirev1.TribefireRuntimeProgressing,
		Status:             core.ConditionTrue,
		LastTransitionTime: meta.NewTime(time.Now()),
		LastUpdateTime:     meta.NewTime(time.Now()),
		Message:            "TribefireRuntime is being created",
		Reason:             "TribefireRuntimeCreated",
	}

	tf.Status.Conditions = append(tf.Status.Conditions, runtimeProgressing)

	tf = updateConditions(tf, true)
	assert.Equal(t, tribefirev1.TribefireRuntimeProgressing, tf.Status.Conditions[0].Type)
	assert.Equal(t, core.ConditionTrue, tf.Status.Conditions[0].Status)
	assert.Equal(t, tribefirev1.TribefireRuntimeAvailable, tf.Status.Conditions[1].Type)
	assert.Equal(t, core.ConditionFalse, tf.Status.Conditions[1].Status)
	assert.Equal(t, "TribefireRuntimeBecameUnavailable", tf.Status.Conditions[1].Reason)
}

func TestHandleExistingProgressingAndAvailableTrueCondition(t *testing.T) {
	tf := &tribefirev1.TribefireRuntime{}
	runtimeProgressing := tribefirev1.TribefireRuntimeCondition{
		Type:               tribefirev1.TribefireRuntimeProgressing,
		Status:             core.ConditionTrue,
		LastTransitionTime: meta.NewTime(time.Now()),
		LastUpdateTime:     meta.NewTime(time.Now()),
		Message:            "TribefireRuntime is being created",
		Reason:             "TribefireRuntimeCreated",
	}

	tf.Status.Conditions = append(tf.Status.Conditions, runtimeProgressing)

	runtimeAvailable := tribefirev1.TribefireRuntimeCondition{
		Type:   tribefirev1.TribefireRuntimeAvailable,
		Status: core.ConditionTrue,
	}

	tf.Status.Conditions = append(tf.Status.Conditions, runtimeAvailable)

	tf = updateConditions(tf, false)
	assert.Equal(t, tribefirev1.TribefireRuntimeProgressing, tf.Status.Conditions[0].Type)
	assert.Equal(t, core.ConditionTrue, tf.Status.Conditions[0].Status)
	assert.Equal(t, tribefirev1.TribefireRuntimeAvailable, tf.Status.Conditions[1].Type)
	assert.Equal(t, core.ConditionTrue, tf.Status.Conditions[1].Status)
	assert.Equal(t, 2, len(tf.Status.Conditions))
}

func TestHandleExistingProgressingAndAvailableFalseCondition(t *testing.T) {
	tf := &tribefirev1.TribefireRuntime{}
	runtimeProgressing := tribefirev1.TribefireRuntimeCondition{
		Type:               tribefirev1.TribefireRuntimeProgressing,
		Status:             core.ConditionTrue,
		LastTransitionTime: meta.NewTime(time.Now()),
		LastUpdateTime:     meta.NewTime(time.Now()),
		Message:            "TribefireRuntime is being created",
		Reason:             "TribefireRuntimeCreated",
	}

	tf.Status.Conditions = append(tf.Status.Conditions, runtimeProgressing)

	runtimeAvailable := tribefirev1.TribefireRuntimeCondition{
		Type: tribefirev1.TribefireRuntimeAvailable,
	}

	tf.Status.Conditions = append(tf.Status.Conditions, runtimeAvailable)

	tf = updateConditions(tf, true)
	assert.Equal(t, tribefirev1.TribefireRuntimeProgressing, tf.Status.Conditions[0].Type)
	assert.Equal(t, core.ConditionTrue, tf.Status.Conditions[0].Status)
	assert.Equal(t, tribefirev1.TribefireRuntimeAvailable, tf.Status.Conditions[1].Type)
	assert.Equal(t, core.ConditionFalse, tf.Status.Conditions[1].Status)
}
