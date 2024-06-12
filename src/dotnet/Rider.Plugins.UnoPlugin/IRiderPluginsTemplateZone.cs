using JetBrains.Application.BuildScript.Application.Zones;
using JetBrains.ProjectModel;

namespace Rider.Plugins.UnoPlugin;

[ZoneMarker]
public class ZoneMarker : IRequire<IProjectModelZone>;