import React, { useEffect } from "react";
import { Layout } from "@/components/layout/Layout/Layout";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { useRefund } from "@/hooks/features/refund/useRefund";
import styles from "./RefundPolicyPage.module.css";

export const RefundPolicyPage: React.FC = () => {
  const { policy, loading, getPolicy } = useRefund();

  useEffect(() => {
    getPolicy();
  }, []);

  return (
    <Layout>
      <div className={styles.page}>
        <div className={styles.container}>
          <h1 className={styles.title}>Refund Policy</h1>

          {loading ? (
            <div className={styles.loading}>
              <LoadingSpinner text="Loading refund policy..." />
            </div>
          ) : policy ? (
            <>
              <div className={styles.rulesGrid}>
                {policy.rules.map((rule, index) => (
                  <div key={index} className={styles.ruleCard}>
                    <div className={styles.ruleHeader}>
                      <h3 className={styles.ruleName}>{rule.name}</h3>
                      <span className={styles.rulePercentage}>
                        {rule.percentage}%
                      </span>
                    </div>
                    <p className={styles.ruleDescription}>{rule.description}</p>
                    <p className={styles.ruleCondition}>{rule.condition}</p>
                  </div>
                ))}
              </div>

              <div className={styles.infoSection}>
                <div className={styles.infoCard}>
                  <h3>Processing Time</h3>
                  <p>{policy.processingTime}</p>
                </div>
                <div className={styles.infoCard}>
                  <h3>Contact Us</h3>
                  <p>{policy.contactEmail}</p>
                </div>
              </div>

              <div className={styles.note}>
                <h3>Important Notes</h3>
                <ul>
                  <li>
                    Refund amount depends on time remaining until the session
                    starts
                  </li>
                  <li>
                    Bonus points used during purchase will be partially refunded
                  </li>
                  <li>
                    Refund will be returned to the original payment method
                  </li>
                  <li>
                    Processing fees may apply based on the refund policy tier
                  </li>
                  <li>You can request a refund from your tickets page</li>
                </ul>
              </div>
            </>
          ) : null}
        </div>
      </div>
    </Layout>
  );
};
