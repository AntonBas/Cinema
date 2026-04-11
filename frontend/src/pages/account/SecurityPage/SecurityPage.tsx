import React, { useState } from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { AccountSidebar } from '@/components/account/AccountSidebar/AccountSidebar';
import { PasswordChangeForm } from '@/components/account/SecuritySection/PasswordChangeForm/PasswordChangeForm';
import { EmailChangeForm } from '@/components/account/SecuritySection/EmailChangeForm/EmailChangeForm';
import styles from './SecurityPage.module.css';

type SecuritySection = 'password' | 'email';

const SECTIONS = [
    { id: 'password' as SecuritySection, label: 'Change Password', icon: '🔑' },
    { id: 'email' as SecuritySection, label: 'Change Email', icon: '📧' },
];

export const SecurityPage: React.FC = () => {
    const [activeSection, setActiveSection] = useState<SecuritySection>('password');

    return (
        <Layout>
            <div className={styles.securityPage}>
                <div className={styles.container}>
                    <AccountSidebar />

                    <div className={styles.content}>
                        <div className={styles.header}>
                            <h1 className={styles.title}>Account Security</h1>
                            <p className={styles.subtitle}>Manage your password and email settings</p>
                        </div>

                        <div className={styles.securityLayout}>
                            <div className={styles.sidebar}>
                                <nav className={styles.securityNav}>
                                    {SECTIONS.map(section => (
                                        <button
                                            key={section.id}
                                            className={`${styles.navButton} ${activeSection === section.id ? styles.active : ''}`}
                                            onClick={() => setActiveSection(section.id)}
                                        >
                                            <span className={styles.navIcon}>{section.icon}</span>
                                            <span className={styles.navLabel}>{section.label}</span>
                                        </button>
                                    ))}
                                </nav>
                            </div>

                            <div className={styles.mainContent}>
                                {activeSection === 'password' ? <PasswordChangeForm /> : <EmailChangeForm />}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
};