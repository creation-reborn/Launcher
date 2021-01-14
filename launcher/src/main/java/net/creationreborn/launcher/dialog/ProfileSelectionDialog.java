/*
 * Copyright 2019 creationreborn.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.creationreborn.launcher.dialog;

import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.FormPanel;
import com.skcraft.launcher.swing.LinedBoxPanel;
import com.skcraft.launcher.swing.PopupMouseAdapter;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;
import net.creationreborn.launcher.auth.Account;
import net.creationreborn.launcher.auth.AccountList;
import net.creationreborn.launcher.auth.AccountType;
import net.creationreborn.launcher.integration.microsoft.MicrosoftIntegration;
import net.creationreborn.launcher.integration.mojang.MojangIntegration;
import net.creationreborn.launcher.integration.mojang.yggdrasil.Profile;
import net.creationreborn.launcher.integration.mojang.yggdrasil.YggdrasilSession;
import net.creationreborn.launcher.util.Progress;
import net.creationreborn.launcher.util.Toolbox;
import org.apache.commons.lang3.StringUtils;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.Callable;

public class ProfileSelectionDialog extends JDialog {

    private final Launcher launcher;
    private final JComboBox<Profile> profiles = new JComboBox<>();
    private final JButton playButton = new JButton(SharedLocale.tr("profileSelection.play"));
    private final JButton addAccountButton = new JButton(SharedLocale.tr("profileSelection.addAccount"));
    private final JButton cancelButton = new JButton(SharedLocale.tr("button.cancel"));
    private final FormPanel formPanel = new FormPanel();
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);
    private boolean cancelled;
    private Session session;

    /**
     * Create a new profile selection dialog.
     *
     * @param owner    the owner
     * @param launcher the launcher
     */
    public ProfileSelectionDialog(Window owner, Launcher launcher) {
        super(owner, ModalityType.DOCUMENT_MODAL);

        this.launcher = launcher;

        setTitle(SharedLocale.tr("profileSelection.title"));
        initComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(420, 0));
        setResizable(false);
        pack();
        setLocationRelativeTo(owner);

        playButton.requestFocusInWindow();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                removeListeners();
                dispose();
            }
        });
    }

    private void removeListeners() {
        profiles.setModel(new DefaultComboBoxModel<>());
    }

    @SuppressWarnings("Duplicates")
    private void initComponents() {
        profiles.setModel(new ProfileListModel(launcher.getAccounts()));
        profiles.setFocusable(false);

        playButton.setFont(playButton.getFont().deriveFont(Font.BOLD));

        formPanel.addRow(new JLabel(SharedLocale.tr("profileSelection.profiles")), profiles);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(26, 13, 13, 13));

        buttonsPanel.addElement(addAccountButton);
        buttonsPanel.addGlue();
        buttonsPanel.addElement(playButton);
        buttonsPanel.addElement(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(playButton);

        profiles.getEditor().getEditorComponent().addMouseListener(new PopupMouseAdapter() {
            @Override
            protected void showPopup(MouseEvent event) {
                popupManageMenu(event.getComponent(), event.getX(), event.getY());
            }
        });

        addAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setResult(false, null);
            }
        });

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                prepareRefresh();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setResult(true, null);
            }
        });
    }

    private void popupManageMenu(Component component, int x, int y) {
        Object selected = profiles.getSelectedItem();
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;

        if (selected instanceof Profile) {
            Profile profile = (Profile) selected;
            Account account = launcher.getAccounts().getAccount(profile).orElse(null);

            if (account != null) {
                menuItem = new JMenuItem(SharedLocale.tr("profileSelection.forgetAccount"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        launcher.getAccounts().remove(account);
                        Persistence.commitAndForget(launcher.getAccounts());
                    }
                });

                popup.add(menuItem);
            }
        }

        popup.show(component, x, y);
    }

    private void prepareRefresh() {
        Object selected = profiles.getSelectedItem();

        if (selected instanceof Profile) {
            Profile profile = (Profile) selected;
            Account account = launcher.getAccounts().getAccount(profile).orElse(null);

            if (account != null && StringUtils.isNotBlank(account.getAccessToken())) {
                attemptRefresh(account);
                return;
            }

            SwingHelper.showErrorDialog(this, SharedLocale.tr("profileSelection.noTokenError"), SharedLocale.tr("profileSelection.noTokenTitle"));
        }

        setResult(false, null);
    }

    private void attemptRefresh(Account account) {
        RefreshCallable callable = new RefreshCallable(account);
        ObservableFuture<Session> future = new ObservableFuture<>(launcher.getExecutor().submit(callable), callable);

        ProgressDialog.showProgress(this, future, SharedLocale.tr("profileSelection.refreshingTitle"), SharedLocale.tr("profileSelection.refreshingStatus"));

        Toolbox.addCallback(future, success -> {
            setResult(false, success);
        }, failure -> {
            setResult(false, null);
        }, SwingExecutor.INSTANCE);

        SwingHelper.addErrorDialogCallback(this, future);
    }

    public void setResult(boolean cancelled, Session session) {
        this.cancelled = cancelled;
        this.session = session;
        removeListeners();
        dispose();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public Session getSession() {
        return session;
    }

    public class ProfileListModel extends AbstractListModel<Profile> implements ComboBoxModel<Profile> {

        private final List<Profile> profiles;
        private Profile selectedProfile;

        public ProfileListModel(AccountList accountList) {
            this.profiles = accountList.getAllProfiles();
            this.selectedProfile = accountList.getCurrentAccount().flatMap(Account::getCurrentProfile).orElse(getElementAt(0));
        }

        @Override
        public void setSelectedItem(Object anItem) {
            if (!(anItem instanceof Profile)) {
                selectedProfile = null;
                return;
            }

            selectedProfile = (Profile) anItem;
        }

        @Override
        public Object getSelectedItem() {
            return selectedProfile;
        }

        @Override
        public int getSize() {
            return profiles.size();
        }

        @Override
        public Profile getElementAt(int index) {
            if (index < 0 || index >= getSize()) {
                return null;
            }

            return profiles.get(index);
        }
    }

    public class RefreshCallable implements Callable<Session>, Progress {

        private final Account account;
        private double progress;
        private String status;

        private RefreshCallable(Account account) {
            this.account = account;
        }

        @Override
        public Session call() throws Exception {
            if (account.getType() == null) {
                account.setType(AccountType.MOJANG);
            }

            if (account.getType() == AccountType.MICROSOFT) {
                if (account.getMicrosoftToken() != null) {
                    MicrosoftIntegration.refresh(account, this);
                } else {
                    MicrosoftIntegration.login(account, this);
                }
            } else if (account.getType() == AccountType.MOJANG) {
                SharedLocale.tr("profileSelection.refreshingStatus");
                MojangIntegration.refresh(account);
            } else {
                throw new UnsupportedOperationException(String.format("%s is not supported", account.getType()));
            }

            launcher.getAccounts().setCurrentAccount(account);
            Persistence.commitAndForget(launcher.getAccounts());
            return account.getCurrentProfile().map(profile -> new YggdrasilSession(account, profile)).orElseThrow(IllegalStateException::new);
        }

        @Override
        public double getProgress() {
            return progress;
        }

        @Override
        public void setProgress(double progress) {
            this.progress = progress;
        }

        @Override
        public String getStatus() {
            return status;
        }

        @Override
        public void setStatus(String status) {
            this.status = status;
        }
    }
}