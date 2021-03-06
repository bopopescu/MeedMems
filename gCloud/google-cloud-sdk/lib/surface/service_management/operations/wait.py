# Copyright 2016 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""service-management operations wait command."""

from googlecloudsdk.api_lib.service_management import base_classes
from googlecloudsdk.api_lib.service_management import services_util

from googlecloudsdk.calliope import base


class Wait(base.Command, base_classes.BaseServiceManagementCommand):
  """Waits for an operation to complete."""

  @staticmethod
  def Args(parser):
    """Args is called by calliope to gather arguments for this command.

    Args:
      parser: An argparse parser that you can use to add arguments that go
          on the command line after this command. Positional arguments are
          allowed.
    """
    parser.add_argument(
        'operation', help='The name of the Operation on which to wait.')

  def Run(self, args):
    """Run 'service-management operations wait'.

    Args:
      args: argparse.Namespace, The arguments that this command was invoked
          with.

    Returns:
      If successful, the response from the operations.Get API call.

    Raises:
      HttpException: An http error response was received while executing api
          request.
    """
    result = services_util.WaitForOperation(
        args.operation, self.services_client)
    return services_util.ProcessOperationResult(result)
