package mil.emp3.test.emp3vv.containers;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.enums.VisibilityActionEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.UpdateContainerDialog;
import mil.emp3.test.emp3vv.containers.dialogs.UpdateFeatureDialog;
import mil.emp3.test.emp3vv.containers.dialogs.UpdateOverlayDialog;
import mil.emp3.test.emp3vv.utils.MapNamesUtility;

public class UpdateContainer extends AddEntityBase implements UpdateContainerDialog.IUpdateContainerDialogListener {

    private static String TAG = UpdateContainer.class.getSimpleName();
    public static final String updateOverlayFragment = "fragment_update_overlay_dialog";
    public static final String updateFeatureFragment = "fragment_update_feature_dialog";

    public UpdateContainer(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        super(activity, map, statusListener, styleManager);
    }

    public void showUpdateContainerDialog(final IMap map, IContainer container, final boolean showOnly) {
        if(container instanceof IOverlay) {
            final IOverlay overlay = (IOverlay) container;
            Handler mainHandler = new Handler(activity.getMainLooper());

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                    UpdateOverlayDialog updateOverlayDialog = UpdateOverlayDialog.newInstance(map, overlay, UpdateContainer.this, showOnly);
                    updateOverlayDialog.show(fm, updateOverlayFragment);
                }
            };
            mainHandler.post(myRunnable);
        } else if(container instanceof IFeature) {
            final IFeature feature = (IFeature) container;
            Handler mainHandler = new Handler(activity.getMainLooper());

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                    UpdateFeatureDialog updateFeatureDialog = UpdateFeatureDialog.newInstance(map, feature, UpdateContainer.this, showOnly);
                    updateFeatureDialog.show(fm, updateFeatureFragment);
                }
            };
            mainHandler.post(myRunnable);
        } else {
            throw new IllegalArgumentException("Expecting IOverlay got " + container.getClass().getSimpleName());
        }
        return;
    }

    @Override
    public void removeMe(UpdateContainerDialog dialog) {
        IContainer removeThis = dialog.getMe();
        try {
            if (removeThis instanceof IOverlay) {
                IOverlay removeThisOverlay = (IOverlay) removeThis;
                List<IContainer> parents = removeThis.getParents();
                for (IContainer container : parents) {
                    if (container instanceof IMap) {
                        ((IMap) container).removeOverlay(removeThisOverlay);
                    } else if (container instanceof IOverlay) {
                        ((IOverlay) container).removeOverlay(removeThisOverlay);
                    }
                }
            } else if(removeThis instanceof IFeature) {
                IFeature removeThisFeature = (IFeature) removeThis;
                List<IContainer> parents = removeThis.getParents();
                for (IContainer container : parents) {
                    if (container instanceof IFeature) {
                        ((IFeature) container).removeFeature(removeThisFeature);
                    } else if (container instanceof IOverlay) {
                        ((IOverlay) container).removeFeature(removeThisFeature);
                    }
                }
            } else {
                statusListener.updateStatus(TAG, "What are you removing? " + removeThis.getClass().getCanonicalName());
            }
        } catch (EMP_Exception e) {
            Log.e(TAG, "removeMe", e);
            statusListener.updateStatus(TAG, e.getMessage());
        }
    }

    @Override
    public boolean updateVisibility(UpdateContainerDialog dialog) {
        boolean returnStatus = true;
        IContainer container = dialog.getMe();
        IMap map = dialog.getMap();
        boolean visibility = dialog.getMeVisible();
        List<String> parentNames = dialog.getSelectedFromMyParentList();

        if((null != parentNames) && (0 != parentNames.size())) {
            for (String name : parentNames) {
                IContainer parentContainer = MapNamesUtility.getContainer(map, name);
                if (parentContainer == null) {
                    Log.e(TAG, "updateVisibility: no container found for name");
                    statusListener.updateStatus(TAG, "updateVisibility: no container found for name");
                } else {
                    try {
                        map.setVisibility(container, parentContainer, (visibility ? VisibilityActionEnum.TOGGLE_OFF.TOGGLE_ON : VisibilityActionEnum.TOGGLE_OFF));
                    } catch (EMP_Exception e) {
                        returnStatus = false;
                        Log.e(TAG, "updateVisibility", e);
                        statusListener.updateStatus(TAG, "updateVisibility: " + e.getMessage());
                    }
                }
            }
        } else {
            try {
                map.setVisibility(container, (visibility ? VisibilityActionEnum.TOGGLE_OFF.TOGGLE_ON : VisibilityActionEnum.TOGGLE_OFF));
            } catch (EMP_Exception e) {
                returnStatus = false;
                Log.e(TAG, "updateVisibility", e);
                statusListener.updateStatus(TAG, "updateVisibility: " + e.getMessage());
            }
        }

        return returnStatus;
    }

    @Override
    public void removeFromParents(UpdateContainerDialog dialog) {
        IMap map = dialog.getMap();
        List<String> parentNames = dialog.getSelectedFromMyParentList();

        if((null != parentNames) && (0 != parentNames.size())) {
            for (String name : parentNames) {
                IContainer parentContainer = MapNamesUtility.getContainer(map, name);
                if (parentContainer == null) {
                    Log.e(TAG, "removeFromParents: no container found for name");
                    statusListener.updateStatus(TAG, "removeFromParents: no container found for name");
                } else {
                    try {
                        if(dialog.getMe() instanceof IOverlay) {
                            IOverlay overlay = (IOverlay) dialog.getMe();
                            if(parentContainer instanceof IMap) {
                                ((IMap)parentContainer).removeOverlay(overlay);
                            } else if(parentContainer instanceof IOverlay) {
                                ((IOverlay) parentContainer).removeOverlay(overlay);
                            } else {
                                Log.e(TAG, "removeFromParents: invalid parent container type " + parentContainer.getClass().getCanonicalName());
                                statusListener.updateStatus(TAG, "removeFromParents: invalid parent container type " + parentContainer.getClass().getCanonicalName());
                            }
                        } else if(dialog.getMe() instanceof IFeature) {
                            IFeature feature = (IFeature) dialog.getMe();
                            if(parentContainer instanceof IFeature) {
                                ((IFeature)parentContainer).removeFeature(feature);
                            } else if(parentContainer instanceof IOverlay) {
                                ((IOverlay) parentContainer).removeFeature(feature);
                            }  else {
                                Log.e(TAG, "removeFromParents: invalid parent container type " + parentContainer.getClass().getCanonicalName());
                                statusListener.updateStatus(TAG, "removeFromParents: invalid parent container type " + parentContainer.getClass().getCanonicalName());
                            }
                        } else {
                            Log.e(TAG, "removeFromParents: invalid container type " + dialog.getMe().getClass().getCanonicalName());
                            statusListener.updateStatus(TAG, "removeFromParents: invalid container type " + dialog.getMe().getClass().getCanonicalName());
                        }

                    } catch (EMP_Exception e) {
                        Log.e(TAG, "removeFromParents", e);
                        statusListener.updateStatus(TAG, "removeFromParents: " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void removeChildren(UpdateContainerDialog dialog) {
        IMap map = dialog.getMap();
        List<String> childrenNames = dialog.getSelectedFromMyChildrenList();

        List<IFeature> featuresToRemove = new ArrayList<>();
        List<IOverlay> overlaysToRemove = new ArrayList<>();
        if((null != childrenNames) && (0 != childrenNames.size())) {
            for (String name : childrenNames) {
                IContainer childContainer = MapNamesUtility.getContainer(map, name);
                if (childContainer == null) {
                    Log.e(TAG, "removeChildren: no container found for name");
                    statusListener.updateStatus(TAG, "removeChildren: no container found for name");
                } else {
                    if(childContainer instanceof IFeature) {
                        featuresToRemove.add((IFeature)childContainer);
                    } else if(childContainer instanceof IOverlay) {
                        overlaysToRemove.add((IOverlay) childContainer);
                    } else {
                        Log.e(TAG, "removeChildren: invalid child container type " + childContainer.getClass().getCanonicalName());
                        statusListener.updateStatus(TAG, "removeChildren: invalid parent container type " + childContainer.getClass().getCanonicalName());
                    }
                }
            }
        }

        try {
            if(dialog.getMe() instanceof IOverlay) {
                // Need to exercise both API single and list
                if(1 == overlaysToRemove.size()) {
                    ((IOverlay) dialog.getMe()).removeOverlay(overlaysToRemove.get(0));
                } else if (1 < overlaysToRemove.size()) {
                    ((IOverlay) dialog.getMe()).removeOverlays(overlaysToRemove);
                }

                if(1 == featuresToRemove.size()) {
                    ((IOverlay) dialog.getMe()).removeFeature(featuresToRemove.get(0));
                } else if (1 < featuresToRemove.size()) {
                    ((IOverlay) dialog.getMe()).removeFeatures(featuresToRemove);
                }
            } else if(dialog.getMe() instanceof IFeature) {
                if(1 == featuresToRemove.size()) {
                    ((IFeature) dialog.getMe()).removeFeature(featuresToRemove.get(0));
                } else if (1 < featuresToRemove.size()) {
                    ((IFeature) dialog.getMe()).removeFeatures(featuresToRemove);
                }
            } else {
                Log.e(TAG, "removeChildren: invalid container type " + dialog.getMe().getClass().getCanonicalName());
                statusListener.updateStatus(TAG, "removeChildren: invalid parent container type " + dialog.getMe().getClass().getCanonicalName());
            }
        } catch(EMP_Exception e) {
            Log.e(TAG, "removeChildren", e);
            statusListener.updateStatus(TAG, "removeChildren: " + e.getMessage());
        }
    }

    @Override
    public void addParents(UpdateContainerDialog dialog) {
        List<String> addParentsNames = dialog.getSelectedFromAddParentsList();

        List<IMap> mapParents = new ArrayList<>();
        List<IFeature> featureParents = new ArrayList<>();
        List<IOverlay> overlayParents = new ArrayList<>();
        if((null != addParentsNames) && (0 != addParentsNames.size())) {
            for (String name : addParentsNames) {
                IContainer parentContainer = MapNamesUtility.getContainer(dialog.getMap(), name);
                if (parentContainer == null) {
                    Log.e(TAG, "addParents: no container found for name");
                    statusListener.updateStatus(TAG, "addParents: no container found for name");
                } else {
                    if(parentContainer instanceof IFeature) {
                        featureParents.add((IFeature)parentContainer);
                    } else if(parentContainer instanceof IOverlay) {
                        overlayParents.add((IOverlay) parentContainer);
                    } else if(parentContainer instanceof IMap) {
                        mapParents.add((IMap) parentContainer);
                    } else {
                        Log.e(TAG, "addParents: invalid child container type " + parentContainer.getClass().getCanonicalName());
                        statusListener.updateStatus(TAG, "addParents: invalid parent container type " + parentContainer.getClass().getCanonicalName());
                    }
                }
            }
        }

        try {
            if(dialog.getMe() instanceof IOverlay) {
                for(IOverlay overlayParent: overlayParents) {
                    overlayParent.addOverlay((IOverlay) dialog.getMe(), true);
                }

                for(IMap mapParent: mapParents) {
                    mapParent.addOverlay((IOverlay) dialog.getMe(), true);
                }
            } else if(dialog.getMe() instanceof IFeature) {
                for(IOverlay overlayParent: overlayParents) {
                    overlayParent.addFeature((IFeature) dialog.getMe(), true);
                }
                for(IFeature featureParent: featureParents) {
                    featureParent.addFeature((IFeature) dialog.getMe(), true);
                }
            } else {
                Log.e(TAG, "addParents: invalid container type " + dialog.getMe().getClass().getCanonicalName());
                statusListener.updateStatus(TAG, "addParents: invalid parent container type " + dialog.getMe().getClass().getCanonicalName());
            }
        } catch(EMP_Exception e) {
            Log.e(TAG, "addParents", e);
            statusListener.updateStatus(TAG, "addParents: " + e.getMessage());
        }
    }

    @Override
    public void addChildren(UpdateContainerDialog dialog) {
        IMap map = dialog.getMap();
        List<String> childrenNames = dialog.getSelectedFromAddChildrenList();

        List<IFeature> featuresToAdd = new ArrayList<>();
        List<IOverlay> overlaysToAdd = new ArrayList<>();
        if((null != childrenNames) && (0 != childrenNames.size())) {
            for (String name : childrenNames) {
                IContainer childContainer = MapNamesUtility.getContainer(map, name);
                if (childContainer == null) {
                    Log.e(TAG, "removeChildren: no container found for name");
                    statusListener.updateStatus(TAG, "removeChildren: no container found for name");
                } else {
                    if(childContainer instanceof IFeature) {
                        featuresToAdd.add((IFeature)childContainer);
                    } else if(childContainer instanceof IOverlay) {
                        overlaysToAdd.add((IOverlay) childContainer);
                    } else {
                        Log.e(TAG, "addChildren: invalid child container type " + childContainer.getClass().getCanonicalName());
                        statusListener.updateStatus(TAG, "addChildren: invalid parent container type " + childContainer.getClass().getCanonicalName());
                    }
                }
            }
        }

        try {
            if(dialog.getMe() instanceof IOverlay) {
                // Need to exercise both API single and list defaulting visibility to true
                if(1 == overlaysToAdd.size()) {
                    ((IOverlay) dialog.getMe()).addOverlay(overlaysToAdd.get(0), true);
                } else if (1 < overlaysToAdd.size()) {
                    ((IOverlay) dialog.getMe()).addOverlays(overlaysToAdd, true);
                }

                if(1 == featuresToAdd.size()) {
                    ((IOverlay) dialog.getMe()).addFeature(featuresToAdd.get(0), true);
                } else if (1 < featuresToAdd.size()) {
                    ((IOverlay) dialog.getMe()).addFeatures(featuresToAdd, true);
                }
            } else if(dialog.getMe() instanceof IFeature) {
                if(1 == featuresToAdd.size()) {
                    ((IFeature) dialog.getMe()).addFeature(featuresToAdd.get(0), true);
                } else if (1 < featuresToAdd.size()) {
                    ((IFeature) dialog.getMe()).addFeatures(featuresToAdd, true);
                }
            } else {
                Log.e(TAG, "addChildren: invalid container type " + dialog.getMe().getClass().getCanonicalName());
                statusListener.updateStatus(TAG, "addChildren: invalid parent container type " + dialog.getMe().getClass().getCanonicalName());
            }
        } catch(EMP_Exception e) {
            Log.e(TAG, "addChildren", e);
            statusListener.updateStatus(TAG, "addChildren: " + e.getMessage());
        }
    }

    @Override
    public void updateName(UpdateContainerDialog dialog) {
        dialog.getMe().setName(dialog.getMeName());
    }
}
