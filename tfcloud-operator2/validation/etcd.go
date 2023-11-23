package validation

import (
	"context"
	"fmt"
	"github.com/google/uuid"
	"github.com/pkg/errors"
	"time"
	tribefirev1 "tribefire-operator/api/v1"
	. "tribefire-operator/common"
	"tribefire-operator/tribefire"

	clientv3 "go.etcd.io/etcd/client/v3"
)

type EtcdChecker struct {
}

func NewEtcdChecker() *EtcdChecker {
	return &EtcdChecker{}
}

func (r *EtcdChecker) Check(tf *tribefirev1.TribefireRuntime) error {
	if tf.Spec.Backend.Type != tribefirev1.EtcdBackend {
		L().Debugf("Skipping etcd validation for non-etcd configured runtime")
		return nil
	}

	etcdUrl := tribefire.GetBackendParam(tf.Spec.Backend.Params, "url")
	if etcdUrl == "" {
		return errors.New(fmt.Sprint("Etcd set as backend for TribefireRuntime, though No etcd URL found"))
	}

	cfg := clientv3.Config{
		Endpoints:   []string{etcdUrl},
		DialTimeout: 2 * time.Second,
	}

	etcdUsername := tribefire.GetBackendParam(tf.Spec.Backend.Params, "username")
	etcdPassword := tribefire.GetBackendParam(tf.Spec.Backend.Params, "password")

	if etcdPassword != "" {
		cfg.Username = etcdUsername
	}

	if etcdPassword != "" {
		cfg.Password = etcdPassword
	}

	c, err := clientv3.New(cfg)
	if err != nil {
		L().Fatal(err)
	}

	kvApi := clientv3.NewKV(c)

	someKey := uuid.New().String()
	someValue := uuid.New().String()

	//// todo check etcd dependencies for this
	//versions, err := c.GetVersion(context.Background())
	//if err != nil {
	//	return err
	//}
	//
	//L().Debugf("Found etcd cluster using URL %s. VersionInfo: cluster=%s server=%s",
	//	etcdUrl, versions.Cluster, versions.Server)

	L().Debugf("setting %s key with %s value", someKey, someValue)
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	_, err = kvApi.Put(ctx, someKey, someValue)
	cancel()
	if err != nil {
		return err
	}

	L().Debugf("getting value for key %s", someKey)
	resp, err := kvApi.Get(context.Background(), someKey)
	if err != nil {
		return err
	}

	defer func() {
		_, err := kvApi.Delete(context.Background(), someKey)
		if err != nil {
			L().Warnf("unable to remove key %s from etcd upon cleanup: %v", someKey, err)
		}

		if err := c.Close(); err != nil {
			L().Errorw("unable to close etcd client", "err", err)
		}

		L().Info("closed etcd client")
	}()

	// expected result
	value := string(resp.Kvs[0].Value)
	if value == someValue {
		return nil
	}

	return errors.New(fmt.Sprintf("etcd check failed. expected=%s actual=%s", someValue, value))
}
